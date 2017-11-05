package it.musichub.server.library.utils;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.NullComparator;

import it.musichub.server.library.utils.SmartBeanComparator.Order;

/**
 * Implementazione di un Comparator.
 * 
 * Con supporto all'ordering, all'ignoreCase (sulle String) e ai campi null.
 * 
 * @author sigitm
 */
public class SmartComparator<T> implements Comparator<T>, Serializable {

	private Comparator<T> comparator;
	private NullComparator nullComparator;
	private Order order = Order.asc;
	private boolean ignoreCase = true;

	public SmartComparator() {
		this(Order.asc, ComparableComparator.getInstance());
	}

	public SmartComparator(Comparator<T> comparator) {
		this(Order.asc, comparator);
	}

	public SmartComparator(Order order) {
		this(order, ComparableComparator.getInstance());
	}

	public SmartComparator(String order) {
		this(orderStringToEnum(order), ComparableComparator.getInstance());
	}

	public SmartComparator(Order order, Comparator<T> comparator) {
		this(order, comparator, true);
	}

	public SmartComparator(String order, Comparator<T> comparator) {
		this(orderStringToEnum(order), comparator, true);
	}
	
	public SmartComparator(Order order, Comparator<T> comparator, boolean ignoreCase) {
		setOrder(order);
		setComparator(comparator);
		setIgnoreCase(ignoreCase);
	}
	
	public SmartComparator(String order, Comparator<T> comparator, boolean ignoreCase) {
		this(orderStringToEnum(order), comparator, ignoreCase);
	}

	private static Order orderStringToEnum(String order) {
		return (order != null && order.equals(Order.desc.toString())) ? Order.desc : Order.asc;
	}

	public Comparator<T> getComparator() {
		return this.comparator;
	}
	
	private void setComparator(Comparator<T> comparator){
		this.comparator = comparator;
		this.nullComparator = new NullComparator(this.comparator, false); //come SQL: ASC->nulls last; DESC->nulls first. 
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

		try {
			if (ignoreCase && obj1 instanceof String && obj2 instanceof String){
				obj1 = (T)((String)obj1).toUpperCase();
				obj2 = (T)((String)obj2).toUpperCase();
			}
			
//			return this.comparator.compare(value1, value2);
			return this.nullComparator.compare(obj1, obj2);
		} catch (Exception e) {
			throw new ClassCastException(e.toString());
		}
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SmartComparator))
			return false;

		SmartComparator<?> beanComparator = (SmartComparator<?>) o;

		if (!(this.comparator.equals(beanComparator.getComparator())))
			return false;
		
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
