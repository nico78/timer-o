/**
 * This class has been generated by Fast Code Eclipse Plugin 
 * For more information please go to http://fast-code.sourceforge.net/
 * @author : ${user}
 * Created : ${today}
 */
package jhc.subidler.testbuilders;

import java.util.Date;

import jhc.subidler.MatchableBuilder;
import lockedstatus.LockRecord;

public class LockRecordBuilder extends
		MatchableBuilder<LockRecord, LockRecordBuilder> {
	public LockRecordBuilder(boolean withInterceptor, Date lockTime,
			Date unlockTime) {
		createNewBean(withInterceptor,
				constructorArgForProperty("lockTime", Date.class, lockTime),
				constructorArgForProperty("unlockTime", Date.class, unlockTime));
	}

	public static LockRecordBuilder lockRecord(Date lockTime, Date unlockTime) {
		return new LockRecordBuilder(false, lockTime, unlockTime);
	}

	public static LockRecordBuilder matchableLockRecord(Date lockTime,
			Date unlockTime) {
		return new LockRecordBuilder(true, lockTime, unlockTime);
	}
}