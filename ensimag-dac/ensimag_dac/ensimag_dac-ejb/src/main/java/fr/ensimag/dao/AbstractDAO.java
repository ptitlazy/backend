package fr.ensimag.dao;

import fr.ensimag.entity.IEntity;
import fr.ensimag.foundation.INames;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.UserTransaction;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @param <E> The type of Entity for this DAO
 */
@TransactionManagement(TransactionManagementType.BEAN)
public abstract class AbstractDAO<E extends IEntity> implements AbstractLocal<E> {
	protected final Class<E> entityClass;

	@PersistenceContext(unitName = INames.PU_NAME)
	private EntityManager entityManager;

	@Resource
	private EJBContext ctx;

	@SuppressWarnings("unchecked")
	protected AbstractDAO() {
		super();
		entityClass = (Class<E>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	protected UserTransaction getUserTransaction() {
		return ctx.getUserTransaction();
	}

	protected E getSingleResult(final CriteriaQuery<E> query) {
		return this.<E>getTypedSingleResult(query);
	}

	protected <T> T getTypedSingleResult(final CriteriaQuery<T> query) {
		try {
			return entityManager.createQuery(query).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	protected List<E> getResultList(final CriteriaQuery<E> query) {
		return entityManager.createQuery(query).getResultList();
	}

	protected List<E> getResultList(final CriteriaQuery<E> query, int maxresults, int firstresult) {
		return entityManager.createQuery(query).setMaxResults(maxresults).setFirstResult(firstresult).getResultList();
	}

	protected CriteriaBuilder getCriteriaBuilder() {
		return entityManager.getCriteriaBuilder();
	}

	@Override
	public E create(final E instance) throws Exception {
		UserTransaction utx = null;
		try {
			utx = getUserTransaction();
			utx.begin();
			entityManager.persist(instance);
			entityManager.flush();
			utx.commit();
			return instance;
		} catch (Exception e) {
			if (utx != null) {
				utx.rollback();
			}
			throw e;
		}
	}

	public E find(Object id) {
		return entityManager.find(entityClass, id);
	}

	public void remove(final E instance) throws Exception {
		UserTransaction utx = null;
		try {
			utx = getUserTransaction();
			utx.begin();
			boolean contains = entityManager.contains(instance);
			E remove = instance;
			if (!contains) {
				remove = find(instance.getId());
			}
			entityManager.remove(remove);
			utx.commit();
		} catch (Exception e) {
			if (utx != null) {
				utx.rollback();
			}
			throw e;
		}
	}

	@Override
	public E edit(final E instance) throws Exception {
		UserTransaction utx = null;
		try {
			utx = getUserTransaction();
			utx.begin();
			E merge = entityManager.merge(instance);
			entityManager.flush();
			utx.commit();
			return merge;
		} catch (Exception e) {
			if (utx != null) {
				utx.rollback();
			}
			throw e;
		}
	}

	public List<E> findAll() {
		CriteriaBuilder builder = getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);
		query.from(entityClass);

		return getResultList(query);
	}

	public int count() {
		return findAll().size();
	}

/*	private Class<T> entityClass;

	public AbstractDAO() {

	}

	public AbstractDAO(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	protected abstract EntityManager getEntityManager();

	protected abstract UserTransaction getUserTransaction();

	public T create(T entity) throws Exception {

		EntityManager em = null;
		UserTransaction utx = null;
		try {
			em = getEntityManager();
			utx = getUserTransaction();
			utx.begin();
			em.persist(entity);
			em.flush();
			utx.commit();
			return entity;
		} catch (Exception e) {
			if (utx != null) {
				utx.rollback();
			}
			throw e;
		}
	}

	public void edit(T entity) throws Exception {
		EntityManager em = null;
		UserTransaction utx = null;
		try {
			em = getEntityManager();
			utx = getUserTransaction();
			utx.begin();
			entity = em.merge(entity);
			em.flush();
			utx.commit();
		} catch (Exception e) {
			if (utx != null) {
				utx.rollback();
			}
			throw e;
		}
	}

	public void remove(T entity) throws Exception {
		EntityManager em = null;
		UserTransaction utx = null;
		try {
			em = getEntityManager();
			utx = getUserTransaction();
			utx.begin();
			em.remove(em.merge(entity));
			em.flush();
			utx.commit();
		} catch (Exception e) {
			if (utx != null) {
				utx.rollback();
			}
			throw e;
		}
	}

	public T find(Object id) throws Exception {
		EntityManager em = null;
		em = getEntityManager();
		try {
			return em.find(entityClass, id);
		} catch (Exception e) {
			throw e;
		}
	}

	public List<T> findAll() throws Exception {
		EntityManager em = getEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(entityClass);
			Root<T> rootEntry = cq.from(entityClass);
			CriteriaQuery<T> all = cq.select(rootEntry);
			TypedQuery<T> allQuery = em.createQuery(all);
			return allQuery.getResultList();
		} catch (Exception e) {
			throw e;
		}
	}

	public int count() throws Exception {
		return findAll().size();
	}*/

}
