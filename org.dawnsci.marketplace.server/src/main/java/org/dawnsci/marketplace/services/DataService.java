/*****************************************************************************
 * Copyright (c) 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Torkild U. Resheim - initial API and implementation
 ****************************************************************************/
package org.dawnsci.marketplace.services;
import java.util.List;

import org.dawnsci.marketplace.Catalogs;
import org.dawnsci.marketplace.Featured;
import org.dawnsci.marketplace.Marketplace;
import org.dawnsci.marketplace.MarketplaceFactory;
import org.dawnsci.marketplace.Node;
import org.dawnsci.marketplace.Search;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Torkild U. Resheim, Itema AS
 */
@Service
public class DataService {
	/**
	 * HQL query to use when searching for a solution using a simple substring
	 * search.
	 */
	private static final String HQL_SEARCH = 
			"select node from Node node where " +
			"lower(node.name) like :term or " +
			"lower(node.owner) like :term or " +
			"lower(str(node.shortdescription)) like :term or " +
			"lower(str(node.body)) like :term " +
			"order by node.changed desc";

	@Autowired
	private SessionFactory sessionFactory;
	
	private String baseUrl = "http://localhost:8080";

	public Marketplace getContent(int identifier) {
		Marketplace marketplace = MarketplaceFactory.eINSTANCE.createMarketplace();
		marketplace.setBaseUrl(baseUrl);
		Session session = sessionFactory.openSession();
		try {
			Query query = session.createQuery("SELECT node FROM Node node WHERE node.id='" + identifier + "'");
			query.setMaxResults(1);
			if (query.list().isEmpty()) {
				return marketplace;
			}
			marketplace.setNode((Node) query.list().get(0));
		} finally {
			session.close();
		}
		return marketplace;
	}

	@SuppressWarnings("unchecked")
	public Marketplace getCatalogs() {
		Marketplace marketplace = MarketplaceFactory.eINSTANCE.createMarketplace();
		marketplace.setBaseUrl(baseUrl);
		Catalogs catalogs = MarketplaceFactory.eINSTANCE.createCatalogs();
		marketplace.setCatalogs(catalogs);
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("SELECT catalog FROM Catalog catalog");
		catalogs.getItems().addAll(query.list());
		session.close();
		return marketplace;
	}

	@SuppressWarnings("unchecked")
	public Marketplace getMarkets() {
		Marketplace marketplace = MarketplaceFactory.eINSTANCE.createMarketplace();
		marketplace.setBaseUrl(baseUrl);
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("SELECT market FROM Market market");
		marketplace.getMarkets().addAll(query.list());
		session.close();
		return marketplace;
	}

	/**
	 * Returns the three features that was last updated.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Marketplace getFeatured() {
		Marketplace marketplace = MarketplaceFactory.eINSTANCE.createMarketplace();
		marketplace.setBaseUrl(baseUrl);
		Featured featured = MarketplaceFactory.eINSTANCE.createFeatured();
		marketplace.setFeatured(featured);
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("SELECT node FROM Node node ORDER BY node.changed ASC");
		query.setMaxResults(3);
		featured.getNodes().addAll(query.list());
		session.close();
		return marketplace;
	}

	/**
	 * Returns the 50 most recent updated solutions
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Marketplace getUpdated() {
		Marketplace marketplace = MarketplaceFactory.eINSTANCE.createMarketplace();
		marketplace.setBaseUrl(baseUrl);
		Featured featured = MarketplaceFactory.eINSTANCE.createFeatured();
		marketplace.setFeatured(featured);
		Session session = sessionFactory.openSession();
		Query query = session.createQuery("SELECT node FROM Node node ORDER BY node.changed ASC");
		query.setMaxResults(50);
		featured.getNodes().addAll(query.list());
		session.close();
		return marketplace;
	}

	@SuppressWarnings("unchecked")
	public Marketplace getSearchResult(String term) {
		Marketplace marketplace = MarketplaceFactory.eINSTANCE.createMarketplace();
		marketplace.setBaseUrl(baseUrl);
		Search search = MarketplaceFactory.eINSTANCE.createSearch();
		marketplace.setSearch(search);
		Session session = sessionFactory.openSession();
		term = term.toLowerCase();
		Query query = session.createQuery(HQL_SEARCH).setString("term", "%"+term+"%");
		List<Node> list = query.list();
		search.getNodes().addAll(list);
		search.setCount(search.getNodes().size());
		search.setTerm(term);
		session.close();
		return marketplace;
	}

}
