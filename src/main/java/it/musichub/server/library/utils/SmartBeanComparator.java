package it.musichub.server.library.utils;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.NullComparator;

/**
 * Implementazione di un BeanComparator.
 * 
 * Ispirato a {@link org.apache.commons.beanutils.BeanComparator}, ma con supporto all'ordering,
 * all'ignoreCase (sulle property String) e ai campi null (anche con nestedProperty null).
 * 
 * @author sigitm
 */
public class SmartBeanComparator<T extends Serializable> implements Comparator<T>, Serializable {

	public static enum Order {asc, desc};

	private String property;
	private Comparator<T> comparator;
	private NullComparator nullComparator;
	private Order order = Order.asc;
	private boolean ignoreCase = true;

	public SmartBeanComparator() {
		this(null);
	}

	public SmartBeanComparator(String property) {
		this(property, Order.asc, ComparableComparator.getInstance());
	}

	public SmartBeanComparator(String property, Comparator<T> comparator) {
		this(property, Order.asc, comparator);
	}

	public SmartBeanComparator(String property, Order order) {
		this(property, order, ComparableComparator.getInstance());
	}

	public SmartBeanComparator(String property, String order) {
		this(property, orderStringToEnum(order), ComparableComparator.getInstance());
	}

	public SmartBeanComparator(String property, Order order, Comparator<T> comparator) {
		this(property, order, comparator, true);
	}

	public SmartBeanComparator(String property, String order, Comparator<T> comparator) {
		this(property, orderStringToEnum(order), comparator, true);
	}
	
	public SmartBeanComparator(String property, Order order, Comparator<T> comparator, boolean ignoreCase) {
		setProperty(property);
		setOrder(order);
		setComparator(comparator);
		setIgnoreCase(ignoreCase);
	}
	
	public SmartBeanComparator(String property, String order, Comparator<T> comparator, boolean ignoreCase) {
		this(property, orderStringToEnum(order), comparator, ignoreCase);
	}

	private static Order orderStringToEnum(String order) {
		return (order != null && order.equals(Order.desc.toString())) ? Order.desc : Order.asc;
	}

	public String getProperty() {
		return this.property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Comparator<T> getComparator() {
		return this.comparator;
	}
	
	private void setComparator(Comparator<T> comparator){
		this.comparator = comparator;
		this.nullComparator = new NullComparator(this.comparator, order == Order.asc); //come SQL: ASC->nulls last; DESC->nulls first. 
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public int compare(T o1, T o2) {
		boolean reverse = (order != null && order == Order.desc);
		T obj1 = reverse ? o2 : o1;
		T obj2 = reverse ? o1 : o2;

		if (this.property == null) {
			return this.comparator.compare(obj1, obj2);
		}
		try {
			Object value1 = null;
			Object value2 = null;
			if (obj1 != null){
				try {
					value1 = PropertyUtils.getProperty(obj1, property);
				} catch (NestedNullException nne) {
					// Nothing to do here
				}
			}
			if (obj2 != null){
				try {
					value2 = PropertyUtils.getProperty(obj2, property);
				} catch (NestedNullException nne) {
					// Nothing to do here
				}
			}
			
			if (ignoreCase && value1 instanceof String && value2 instanceof String){
				value1 = ((String)value1).toUpperCase();
				value2 = ((String)value2).toUpperCase();
			}
			
//			return this.comparator.compare(value1, value2);
			return this.nullComparator.compare(value1, value2);
		} catch (Exception e) {
			throw new ClassCastException(e.toString());
		}
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SmartBeanComparator))
			return false;

		SmartBeanComparator<?> beanComparator = (SmartBeanComparator<?>) o;

		if (!(this.comparator.equals(beanComparator.getComparator())))
			return false;
		if (this.property != null) {
			if (!(this.property.equals(beanComparator.getProperty())))
				return false;
		} else {
			if (beanComparator.getProperty() != null)
				return false;
		}
		
		if (this.order != null && this.order == Order.desc){
			if (!(beanComparator.getOrder() != null && this.getOrder() == Order.desc)){
				return false;
			}
		}

		return true;
	}

	public int hashCode() {
		int result = this.comparator.hashCode();
		return result;
	}

}
