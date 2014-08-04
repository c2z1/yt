package de.yourtasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

@Api(name = "taskendpoint", namespace = @ApiNamespace(ownerDomain = "yourtasks.de", ownerName = "yourtasks.de", packagePath = ""))
public class TaskEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listTask")
	public CollectionResponse<Task> listTask(
			@Nullable @Named("projectId") Long projectId,
			@Nullable @Named("withCompleted") Boolean withCompleted,
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Task> execute = null;

		try {
			mgr = getEntityManager();
			StringBuilder sb = new StringBuilder("select t from Task t");
			
			Collection<String> whereClauses = new ArrayList<String>();
			if (Boolean.FALSE.equals(withCompleted)) {
				whereClauses.add("t.completed IS NULL");
			}
			if (projectId != null) {
				if (projectId >= 0) {
					whereClauses.add("t.projectId = :projectId");
				}
			} else {
				sb.append("t.projectId IS NULL");
			}
			Iterator<String> it = whereClauses.iterator();
			if (it.hasNext()) {
				sb.append(" where ");
				sb.append(it.next());
			}
			while (it.hasNext()) {
				sb.append(" AND ");
				sb.append(it.next());
			}
			
			sb.append(" order by t.prio");
			
			Query query = mgr.createQuery(sb.toString());
			
			if (projectId != null && projectId >= 0) {
				query.setParameter("projectId", projectId);  // where t.projectId = :projectId 
			}
			
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Task>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Task obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Task> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getTask")
	public Task getTask(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		Task task = null;
		try {
			task = mgr.find(Task.class, id);
		} finally {
			mgr.close();
		}
		return task;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param task the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertTask")
	public Task insertTask(Task task) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsTask(task)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(task);
		} finally {
			mgr.close();
		}
		return task;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param task the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateTask")
	public Task updateTask(Task task) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsTask(task)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(task);
		} finally {
			mgr.close();
		}
		return task;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "convertModel")
	public void convertModel(@Named("id") Long id) {
		EntityManager mgr = null;
		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select t from Task t where t.id = ':taskId");
			
			query.setParameter("taskId", id);  
			
			List<Task> list = (List<Task>) query.getResultList();
			
			
			for (Task task : list) {
				Long tmpid = task.getProjectId();
				task.setParentTaskId(tmpid + 1);
				mgr.persist(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		} finally {
			mgr.close();
			
		}
	}
	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "convertProjects")
	public void convertProjects(@Named("id") Long id) {
		EntityManager mgr = null;
		try {
			mgr = getEntityManager();
			
			Query query = mgr.createQuery("select p from Project p where p.id = :projectid");
			query.setParameter("projectid", id);  
			List<Project> projectlist = (List<Project>) query.getResultList();
			for (Project project : projectlist) {
				Task t = new Task();
				t.setId(project.getId() + 1);
				t.setName(project.getName());
				t.setPrio(3);
				mgr.persist(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		} finally {
			mgr.close();
			
		}
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeTask")
	public void removeTask(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		try {
			Task task = mgr.find(Task.class, id);
			mgr.remove(task);
		} finally {
			mgr.close();
		}
	}

	private boolean containsTask(Task task) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Task item = mgr.find(Task.class, task.getId());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

}
