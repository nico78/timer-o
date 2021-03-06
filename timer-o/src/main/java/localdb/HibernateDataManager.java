package localdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hsqldb.DatabaseManager;

import core.Timero;

public class HibernateDataManager implements DataManager {

	private SessionFactory sessionFactory;
	
	/* (non-Javadoc)
	 * @see localdb.DataManager#init()
	 */
	@Override
	public void init(){
		sessionFactory = new Configuration()
        .configure() // configures settings from hibernate.cfg.xml
        .buildSessionFactory();
	}
	
	
	/* (non-Javadoc)
	 * @see localdb.DataManager#save(java.lang.Object)
	 */
	@Override
	public  void save(Object obj) {
		Session session = openSession();
		session.beginTransaction();
		session.saveOrUpdate(obj);
		session.getTransaction().commit();
		session.close();
	}

	/* (non-Javadoc)
	 * @see localdb.DataManager#save(java.lang.Object)
	 */
	@Override
	public void save(Object... objs){
		Session session = openSession();
		session.beginTransaction();
		for(Object obj:objs){
			session.saveOrUpdate(obj);
		}
		session.getTransaction().commit();
		session.close();
	}
	@Override
public void refresh(Object... objs){
	Session session = openSession();
	for(Object obj:objs){
		session.refresh(obj);
	}
	session.close();
}
	
	@Override
	public void deleteActivity(ActivityRecord activity){
		System.out.println("Deleting " +activity);
		Session session = openSession();
		session.beginTransaction();
		session.delete(activity);
		session.getTransaction().commit();
		
		session.flush();
		session.close();
	}
	
	
	/* (non-Javadoc)
	 * @see localdb.DataManager#getJobByReference(java.lang.String)
	 */
	@Override
	public  Job getJobByReference(String reference) {
		Session session = openSession();
		Query query = session.createQuery("from Job where reference = :reference");
		query.setParameter("reference", reference);
		Job job = (Job) query.uniqueResult();
		session.close();
		return job;		
	}

	private Session openSession() {
		if(sessionFactory==null)
			throw new IllegalStateException("Session factory not initialized");
		Session session = sessionFactory.openSession();
		return session;
	}
	
	/* (non-Javadoc)
	 * @see localdb.DataManager#getJobs(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public  List<Job> getJobs(String filter){
		Session session = openSession();
		Query query = session.createQuery("from Job where description like :descFilter or reference like :referenceFilter");
		query.setParameter("descFilter", like(filter));
		query.setParameter("referenceFilter", like(filter));
		List<Job> list = (List<Job>)query.list();
		session.close();
		return list;
	}

	private static String like(String filter) {
		return "%"+filter+"%";
	}

	/* (non-Javadoc)
	 * @see localdb.DataManager#getTasksByDescription(java.lang.String)
	 */
	@Override
	public Task getTasksByDescription(String filter) {//temporary - will get rid of
		Session session = openSession();
		Query query = session.createQuery("from Task where taskDescription like :descFilter");
		query.setParameter("descFilter",like(filter));
		Task uniqueResult = (Task) query.uniqueResult();
	//	session.close();
		return uniqueResult;
	}
	
	public static DataManager create(){
			DataManager dataManager = new HibernateDataManager();
			dataManager.init();
			return dataManager;
	}

	/* (non-Javadoc)
	 * @see localdb.DataManager#getAllJobs()
	 */
	@Override
	public List<Job> getAllJobs() {
		Session session = openSession();
		Query query = session.createQuery("from Job ");
		List<Job> list = (List<Job>) query.list();
		session.close();
		return list;
	}

	@Override
	public List<ActivityRecord> activity(Date fromTime, Date toTime) {
		Session session = openSession();
		Query query = session.createQuery("from ActivityRecord where starttime > :fromTime and endtime < :toTime order by starttime");
		query.setDate("fromTime", fromTime);
		query.setDate("toTime", toTime);
		List<ActivityRecord> list = (List<ActivityRecord>)query.list();
		session.close();
		return list;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public List runQuery(String queryString){
		Session session = openSession();
		Query query = session.createQuery(queryString);
		List list = query.list();
		session.close();
		return list;
	}
	
	private  List<Task> getStandardTasks(Job job){
		List<Task> standardTasks= new ArrayList<Task>();
		for(String name :Arrays.asList("Release notes","Coding", "Debugging","Analysis","Unit tests","Dev tests","Support QA", "Scheduling", "Builds", "Data setup")){
			standardTasks.add(new Task(job, name));
		}
		return standardTasks;
	}

	@Override
	public List<Task> getSuggestedTasksForJob(Job job) {
		List<Task> recordedTasks = getRecordedTasksFor(job);
		List<Task> standardTasks = getStandardTasks(job);
		
		List<Task> suggestedTasks = new ArrayList<Task>();
		Set<String> recNames = new HashSet<String>();
		for(Task recTask:recordedTasks){
			recNames.add(recTask.getTaskDescription());
			suggestedTasks.add(recTask);
		}
		for(Task standardTask: standardTasks){
			if(!recNames.contains(standardTask.getTaskDescription()))
					suggestedTasks.add(standardTask);
		}
		return suggestedTasks;
	}


	public List<Task> getRecordedTasksFor(Job job) {
		Session session = openSession();
		Query query = session.createQuery("from Task where job = :job");
		query.setParameter("job", job);
		List<Task> recordedTasks = (List<Task>)query.list();
		session.close();
		return recordedTasks;
	}


	@Override
	public void close() {
		sessionFactory.close();
		System.out.println("session factory closed: " + sessionFactory.isClosed());
		
		DatabaseManager.closeDatabases(0);

	}

	
	
}
