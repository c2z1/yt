package de.yourtasks;

import de.yourtasks.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "projectendpoint", namespace = @ApiNamespace(ownerDomain = "yourtasks.de", ownerName = "yourtasks.de", packagePath = ""))
public class ProjectEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listProject")
	public CollectionResponse<Project> listProject(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Project> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select from Project as Project");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Project>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Project obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Project> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getProject")
	public Project getProject(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		Project project = null;
		try {
			project = mgr.find(Project.class, id);
		} finally {
			mgr.close();
		}
		return project;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param project the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertProject")
	public Project insertProject(Project project) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsProject(project)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(project);
		} finally {
			mgr.close();
		}
		return project;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param project the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateProject")
	public Project updateProject(Project project) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsProject(project)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(project);
		} finally {
			mgr.close();
		}
		return project;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeProject")
	public void removeProject(@Named("id") Long id) {
		EntityManager mgr = getEntityManager();
		try {
			Project project = mgr.find(Project.class, id);
			mgr.remove(project);
		} finally {
			mgr.close();
		}
	}

	private boolean containsProject(Project project) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Project item = mgr.find(Project.class, project.getId());
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
