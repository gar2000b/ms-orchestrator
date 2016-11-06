package com.onlineinteract.core.component;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.onlineinteract.core.Workspace;
import com.onlineinteract.core.workbench.Template;

public class ServiceList {

	@SuppressWarnings("unused")
	private Workspace workspace;
	private com.badlogic.gdx.scenes.scene2d.ui.List<String> serviceList;
	private List<String> orderedServiceList;
	ScrollPane scrollPane;
	private List<Template> templateInstances = new ArrayList<Template>();

	public ServiceList(Workspace workspace) {
		this.workspace = workspace;
		serviceList = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(workspace.getSkin());
		orderedServiceList = new ArrayList<>();

		serviceList.setItems(orderedServiceList.toArray(new String[orderedServiceList.size()]));
		scrollPane = new ScrollPane(serviceList);
		scrollPane.setBounds(10, 10, 200, 200);
		scrollPane.setSmoothScrolling(true);
		scrollPane.setPosition(workspace.getWorldWidth() - 280, 20);
		scrollPane.setTransform(true);
		scrollPane.setScale(1);
		serviceList.setColor(Color.CYAN);
		serviceList.getSelection().setMultiple(true);
		serviceList.getSelection().setRequired(false);
		serviceList.setSelected(null);
		serviceList.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (serviceList.getSelectedIndex() == 3) {
					serviceList.clearItems();
					orderedServiceList.remove(3);
					serviceList.setItems(orderedServiceList.toArray(new String[orderedServiceList.size()]));
					serviceList.setSelectedIndex(4);
				}
			}
		});

		workspace.getStage().addActor(scrollPane);
	}

	public void addTemplateInstance(Template instance) {
		templateInstances.add(instance);
		if (instance.getLabel().equals("Áicroservice"))
			orderedServiceList.add("New Service (UUID = " + instance.getUuid() + ")");
		else
			orderedServiceList.add(instance.getLabel() + " (UUID = " + instance.getUuid() + ")");
		serviceList.clearItems();
		serviceList.setItems(orderedServiceList.toArray(new String[orderedServiceList.size()]));
	}

	public void updateServiceList(Template instance) {
		for (int i = 0; i < orderedServiceList.size(); i++) {
			if (orderedServiceList.get(i).contains(instance.getUuid().toString()))
				orderedServiceList.set(i, instance.getLabel() + " (UUID = " + instance.getUuid() + ")");
		}
		serviceList.clearItems();
		serviceList.setItems(orderedServiceList.toArray(new String[orderedServiceList.size()]));
	}

	public List<Template> getTemplateInstances() {
		return templateInstances;
	}
}
