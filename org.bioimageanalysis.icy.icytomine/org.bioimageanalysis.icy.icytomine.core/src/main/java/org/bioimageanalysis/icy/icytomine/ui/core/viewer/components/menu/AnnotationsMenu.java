package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;

import be.cytomine.client.CytomineException;

@SuppressWarnings("serial")
public class AnnotationsMenu extends JMenu {

	public interface UserFilterListener {
		void userSelectionChange(User user, boolean selected);
	}

	public interface TermFilterListener {
		void termSelectionChange(Term term, boolean selected);
	}

	private Image imageInformation;
	private JMenu menuUserFilter;
	private JMenu menuTermFilter;
	private Set<UserFilterListener> userListeners;
	private Set<TermFilterListener> termListeners;

	public AnnotationsMenu(Image imageInformation) {
		super("Annotations");
		this.imageInformation = imageInformation;
		this.userListeners = new HashSet<>();
		this.termListeners = new HashSet<>();
		buildMenuUserFilter();
		buildMenuTermFilter();
		add(menuUserFilter);
		add(menuTermFilter);
	}

	private void buildMenuUserFilter() {
		menuUserFilter = new JMenu("Filter users");

		if (imageInformation != null) {
			List<User> users = getImageAnnotationUsers(imageInformation);
			addUserFilterMenuItems(users);
			addAllUserFilterMenuItem();
		}
	}

	private List<User> getImageAnnotationUsers(Image imageInformation) {
		List<User> users;
		try {
			users = imageInformation.getAnnotationUsers();
		} catch (CytomineException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
			users = new ArrayList<>(0);
		}
		return users;
	}

	private void addUserFilterMenuItems(List<User> users) {
		users.stream().forEach(user -> {
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(user.getUserName());
			menuItem.setSelected(true);
			menuItem.addActionListener(e -> userFilterChanged(user, ((AbstractButton) e.getSource()).isSelected()));
			menuUserFilter.add(menuItem);
		});
	}

	private void userFilterChanged(User user, boolean selected) {
		userListeners.forEach(listener -> listener.userSelectionChange(user, selected));
		System.out.println("User " + user.getUserName() + " has been " + (selected ? "selected" : "unselected"));
	}

	private void addAllUserFilterMenuItem() {
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("All");
		menuItem.setSelected(true);
		menuItem.addActionListener(e -> allUserFilterChanged(((AbstractButton) e.getSource()).isSelected()));
		menuUserFilter.add(menuItem);
	}

	private void allUserFilterChanged(boolean isSelected) {
		System.out.println("All Users " + " have been " + (isSelected ? "selected" : "unselected"));
		for (int i = 0; i < menuUserFilter.getItemCount(); i++) {
			JCheckBoxMenuItem item = ((JCheckBoxMenuItem) (menuUserFilter.getItem(i)));
			if (item.isSelected() != isSelected)
				item.doClick();
		}
	}

	private void buildMenuTermFilter() {
		menuTermFilter = new JMenu("Filter Term");

		if (imageInformation != null) {
			List<Term> terms = getImageAvailableTerms(imageInformation);
			addTermFilterMenuItems(terms);
			addAllTermFilterMenuItem();
		}
	}

	private List<Term> getImageAvailableTerms(Image imageInformation) {
		List<Term> terms;
		try {
			terms = imageInformation.getAvailableTerms();
		} catch (CytomineException e) {
			e.printStackTrace();
			terms = new ArrayList<>(0);
		}
		return terms;
	}

	private void addTermFilterMenuItems(Collection<Term> terms) {
		terms.stream().forEach(term -> {
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(term.getName());
			menuItem.setSelected(true);
			menuItem.addActionListener(e -> termFilterChanged(term, ((AbstractButton) e.getSource()).isSelected()));
			menuTermFilter.add(menuItem);
		});
	}

	private void termFilterChanged(Term term, boolean selected) {
		termListeners.forEach(listener->listener.termSelectionChange(term, selected));
		System.out.println("Term " + term.getName() + " has been " + (selected ? "selected" : "unselected"));
	}

	private void addAllTermFilterMenuItem() {
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("All");
		menuItem.setSelected(true);
		menuItem.addActionListener(e -> allTermFilterChanged(((AbstractButton) e.getSource()).isSelected()));
		menuTermFilter.add(menuItem);
	}

	private void allTermFilterChanged(boolean isSelected) {
		System.out.println("All Terms " + " have been " + (isSelected ? "selected" : "unselected"));
		for (int i = 0; i < menuTermFilter.getItemCount(); i++) {
			JCheckBoxMenuItem item = ((JCheckBoxMenuItem) (menuTermFilter.getItem(i)));
			if (item.isSelected() != isSelected)
				item.doClick();
		}
	}

	public void addUserFilterListener(UserFilterListener listener) {
		this.userListeners.add(listener);
	}

	public void addTermFilterListener(TermFilterListener listener) {
		this.termListeners.add(listener);
	}
}
