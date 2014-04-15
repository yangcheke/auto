package com.autoboys.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.googlecode.s2hibernate.struts2.plugin.annotations.SessionTarget;
import com.googlecode.s2hibernate.struts2.plugin.annotations.TransactionTarget;
import com.autoboys.domain.*;
import com.autoboys.util.ProxoolConnection;
import com.autoboys.util.StringUtil;

public class ProductDAOImpl implements ProductDAO {
	
	@SessionTarget
	Session session;
	
	@TransactionTarget
	Transaction transaction;

	/**
	 * Used to save or update a product.
	 */
	public void saveOrUpdateProduct(Product product) {
		try {
			session.saveOrUpdate(product);
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}
	}

	/**
	 * Used to delete a product.
	 */
	
	public void deleteProduct(Long productId) {
		try {
			Product product = (Product) session.get(Product.class, productId);
			session.delete(product);
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		} 
	}
	
	/**
	 * Used to list all the products.
	 */
	@SuppressWarnings("unchecked")
	public List<Product> listProduct() {
		List<Product> courses = null;
		try {
			courses = session.createQuery("from Product").list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return courses;
	}

	/**
	 * Used to list a single product by Id.
	 */
	
	public Product listProductById(Long productId) {
		Product product = null;
		try {
			product = (Product) session.get(Product.class, productId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return product;
	}
	
	public int qryProductsCount(Product cnds,String selCat,String brand,String series){
		int ret = 0;
		try {
			StringBuilder sb =  new StringBuilder("select count(1) from product t1 where 1=1 ");
			if(cnds.getService_id() != null && !"".equals(cnds.getService_id())) {
				sb.append(" and t1.service_id='").append(cnds.getService_id()).append("' ");
			} else {
				if(selCat !=null && !"".equals(selCat)) {
					sb.append(" and exists (select 1 from SERVICE t2 where t1.service_id=t2.code and CATEGORY_CODE ='").append(selCat).append("') ");
				}
			}
			if(series!=null&&!"".equals(series)) {
				sb.append(" and exists (select 1 from PRODUCT_VEHICLE a join VEHICLE b on a.PRODUCT_ID=t1.id and a.VEHICLE_ID=b.id and b.SERIES_CODE ='")
					.append(series).append("' ) ");
			} else {
				if(brand!=null&&!"".equals(brand)) {
					sb.append(" and exists (select 1 from PRODUCT_VEHICLE a join VEHICLE b on a.PRODUCT_ID=t1.id and a.VEHICLE_ID=b.id and b.BRAND_CODE ='")
					.append(brand).append("' ) ");
				}
			}
			if(cnds.getName() != null && !"".equals(cnds.getName())) {
				sb.append(" and t1.name like '%").append(cnds.getName()).append("%' ");
			}
			
			Query q = session.createSQLQuery(sb.toString());
			ret = ((java.math.BigDecimal)q.uniqueResult()).intValue();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public List<Product> qryProductsList(Product cnds,String selCat,String brand,String series, int pageNo ,int pageSize){
		List<Product> list = new ArrayList<Product>();
		try {
			java.sql.Connection conn = null;
			try {
				StringBuilder sb =  new StringBuilder("select b.*,(select name from PRODUCT_BRAND where id=b.brand_id and rownum=1) brandName  from (select a.*,rownum rn from (select t1.* from product t1 where 1=1 ");
				if(cnds.getService_id() != null && !"".equals(cnds.getService_id())) {
					sb.append(" and t1.service_id='").append(cnds.getService_id()).append("' ");
				} else {
					if(selCat !=null && !"".equals(selCat)) {
						sb.append(" and exists (select 1 from SERVICE t2 where t1.service_id=t2.code and CATEGORY_CODE ='").append(selCat).append("') ");
					}
				}
				if(series!=null&&!"".equals(series)) {
					sb.append(" and exists (select 1 from PRODUCT_VEHICLE a join VEHICLE b on a.PRODUCT_ID=t1.id and a.VEHICLE_ID=b.id and b.SERIES_CODE ='")
						.append(series).append("' ) ");
				} else {
					if(brand!=null&&!"".equals(brand)) {
						sb.append(" and exists (select 1 from PRODUCT_VEHICLE a join VEHICLE b on a.PRODUCT_ID=t1.id and a.VEHICLE_ID=b.id and b.BRAND_CODE ='")
						.append(brand).append("' ) ");
					}
				}
				if(cnds.getName() != null && !"".equals(cnds.getName())) {
					sb.append(" and t1.name like '%").append(cnds.getName()).append("%' ");
				}
				sb.append("order by id) a where rownum<=?) b where rn >?");
				conn = ProxoolConnection.getConnection();
		        QueryRunner qRunner = new QueryRunner();   
		        list = (List<Product>) qRunner.query(conn, sb.toString(), new BeanListHandler(Product.class),pageNo*pageSize,(pageNo-1)*pageSize);
		        
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				try {conn.close();}catch(Exception e){}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
