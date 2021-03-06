package core;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class CBRController {
	
	private final Preferences prefRoot;
	private List<CBRProject> projects;
	
	public CBRController() {
		this.projects = new ArrayList<CBRProject>();
		this.prefRoot = Preferences.userNodeForPackage(CBRController.class);
		
		// Read project data from registry.
		try {
			String[] projectNames = prefRoot.childrenNames();
			for (String pName : projectNames) {
				try {
					projects.add(new CBRProject(pName, prefRoot.node(pName).get("url", "Registry error.")));
				} catch (Exception e) {
					// The user probably deleted the resource
					prefRoot.node(pName);
				}
			}
		} catch (BackingStoreException bse) {
			bse.printStackTrace();
		}
		
	}
	
	public void add(CBRProject p) {
		projects.add(p);
		prefRoot.node(p.getName()).put("url", p.getURL());
	}
	
	public void remove(String pName) {
		// Removes project data from registry.
		try {
			prefRoot.node(pName).removeNode();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		
		// Removes project from project list.
		for (int i = 0; i < projects.size(); i++) {
			if(projects.get(i).getName().equals(pName)) {
				projects.remove(i);
				return;
			}
		}
	}
	
	public List<CBRProject> getProjects() {
		return this.projects;
	}
	
	public List<String> getProjectNames() {
		int pcount = this.projects.size();
		List<String> names = new ArrayList<String>();
		for(int i = 0; i < pcount; i++) {
			names.add(this.projects.get(i).getName());
		}
		
		return names;
	}
}
