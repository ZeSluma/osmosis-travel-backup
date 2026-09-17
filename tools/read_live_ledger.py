"""Read-only live ledger projection. Database/WAL bytes stay in RAM; no app restart."""
import hashlib, json, os, sqlite3, struct, subprocess, sys

def checksum(data, order, state=(0, 0)):
    words = struct.unpack(order + str(len(data)//4) + 'I', data)
    a, b = state
    for i in range(0, len(words), 2):
        a = (a + words[i] + b) & 0xffffffff
        b = (b + words[i+1] + a) & 0xffffffff
    return a, b

def image_with_wal(base, wal):
    image = bytearray(base)
    assert image[:16] == b'SQLite format 3\x00'
    if wal:
        assert len(wal) >= 32
        magic, version, page = struct.unpack('>III', wal[:12])
        assert magic in (0x377f0682, 0x377f0683) and version == 3007000
        assert page == (65536 if image[16:18] == b'\x00\x01' else int.from_bytes(image[16:18], 'big'))
        order = '<' if magic == 0x377f0682 else '>'
        state = checksum(wal[:24], order)
        assert state == struct.unpack('>II', wal[24:32])
        frames, last_commit = [], None
        for pos in range(32, len(wal)-23-page, 24+page):
            header, data = wal[pos:pos+24], wal[pos+24:pos+24+page]
            if header[8:16] != wal[16:24]: break
            candidate = checksum(header[:8]+data, order, state)
            if candidate != struct.unpack('>II', header[16:24]): break
            state = candidate
            number, size = struct.unpack('>II', header[:8])
            assert number > 0
            frames.append((number, data))
            if size: last_commit = (len(frames), size)
        if last_commit:
            count, size = last_commit
            assert size * page < 64*1024*1024
            image.extend(b'\x00' * max(0, size*page-len(image)))
            for number, data in frames[:count]:
                if number <= size: image[(number-1)*page:number*page] = data
            del image[size*page:]
    # Only the RAM analysis image uses rollback-header mode; device bytes never change.
    image[18:20] = b'\x01\x01'
    db = sqlite3.connect(':memory:')
    db.deserialize(bytes(image))
    assert db.execute('PRAGMA integrity_check').fetchone() == ('ok',)
    db.execute('PRAGMA query_only=ON')
    return db

def self_test():
    import tempfile
    with tempfile.TemporaryDirectory(prefix='osmosis-synthetic-wal-') as directory:
        path = os.path.join(directory, 'fixture.db')
        original = sqlite3.connect(path)
        original.execute('PRAGMA journal_mode=WAL')
        original.execute('CREATE TABLE fixture (n INTEGER)')
        original.execute('INSERT INTO fixture VALUES (3)'); original.commit()
        original.execute('PRAGMA user_version=6'); original.commit()
        base = open(path,'rb').read(); wal = open(path+'-wal','rb').read()
        analysis = image_with_wal(base,wal)
        assert analysis.execute('SELECT n FROM fixture').fetchone() == (3,)
        assert analysis.execute('PRAGMA user_version').fetchone() == (6,)
        analysis.close(); original.close()
    print('PASS: checksum-validated WAL commit reconstructed in RAM')

def audit():
    adb = os.path.join(os.environ['TEMP'],'osmosis-gate0-toolchain','android-sdk','platform-tools','adb.exe')
    def read(suffix):
        command = 'if [ -f no_backup/sync-ledger.db%s ]; then cat no_backup/sync-ledger.db%s; fi' % (suffix,suffix)
        p = subprocess.run([adb,'-d','exec-out','run-as','dev.konraditurbe.osmosis','sh','-c',command],capture_output=True,timeout=15)
        assert p.returncode == 0 and not p.stderr and len(p.stdout)<64*1024*1024
        return p.stdout
    first = (read(''),read('-wal')); second = (read(''),read('-wal'))
    assert first == second, 'UNSTABLE_SNAPSHOT'
    db=image_with_wal(*first)
    schema = db.execute('PRAGMA user_version').fetchone()[0]
    assert schema in (6, 7)
    opaque=lambda s: hashlib.sha256(s.encode()).hexdigest() if s else None
    allowed={'INCOMPLETE','COMPLETE','DISCOVERED','NEEDS_REVALIDATION','LOCAL_PRESENT_UNVERIFIED','TRANSFERRED_UNVERIFIED','LOCAL_VERIFIED','PARTIAL','PRESENT_UNVERIFIED','ABSENT','AMBIGUOUS','NOT_SCANNED','MISSING','UNAVAILABLE','CHANGED','INTENT','ALLOCATING','WRITING','PUBLISH_PENDING','PUBLISHED','CONFIRMED','UNCONFIRMED','FAILED'}
    token=lambda s:s if s in allowed else 'UNRECOGNIZED'
    tables = ['assets','snapshots','identity_observations','transfer_attempts','transfer_integrity','source_equivalence']
    if schema == 7:
        tables.append('resume_evidence')
    result={'schema':schema,'capture':'STABLE_DOUBLE_READ_CHECKSUM_VALIDATED_WAL_IN_RAM','counts':{t:db.execute('SELECT COUNT(*) FROM '+t).fetchone()[0] for t in tables}}
    result['assets']=[]
    for r in db.execute("SELECT a.id,a.size,p.state,p.localPresence,p.localLocator,p.committedLength,a.identityAmbiguous,a.strongVersion IS NOT NULL FROM assets a JOIN replicas p ON a.id=p.assetId WHERE p.destination='PHONE_LOCAL' ORDER BY a.id"):
        result['assets'].append(dict(asset=opaque(r[0]),bytes=r[1],state=token(r[2]),presence=token(r[3]),locator=opaque(r[4]),committed=r[5],ambiguous=bool(r[6]),source_version_present=bool(r[7])))
    result['latest']=[]
    for r in db.execute('SELECT s.id,s.status,(SELECT COUNT(*) FROM membership m WHERE m.snapshotId=s.id) FROM snapshots s JOIN sources x ON x.id=s.sourceId WHERE s.ownerEpoch=x.ownerEpoch'):
        result['latest'].append(dict(snapshot=opaque(r[0]),status=token(r[1]),members=r[2]))
    result['attempts']=[dict(asset=opaque(r[0]),state=token(r[1]),checkpoint=r[2],expected=r[3]) for r in db.execute('SELECT assetId,state,checkpoint,expectedBytes FROM transfer_attempts')]
    result['latest_observations']=[dict(observation=opaque(r[0]),bytes=r[1],resolved=bool(r[2]),source_version_present=bool(r[3])) for r in db.execute('SELECT o.id,o.size,o.resolvedAssetId IS NOT NULL,o.strongVersion IS NOT NULL FROM identity_observations o JOIN snapshots s ON s.id=o.snapshotId JOIN sources x ON x.id=s.sourceId WHERE s.ownerEpoch=x.ownerEpoch')]
    result['integrity']=[dict(asset=opaque(r[0]),result=token(r[1]),bytes=r[2]) for r in db.execute('SELECT assetId,result,expectedBytes FROM transfer_integrity')]
    db.close(); print(json.dumps(result))

if __name__=='__main__':
    try:
        self_test() if '--self-test' in sys.argv else audit()
    except Exception:
        print('BLOCKED_SANITIZED_LIVE_LEDGER_PROJECTION'); sys.exit(1)
