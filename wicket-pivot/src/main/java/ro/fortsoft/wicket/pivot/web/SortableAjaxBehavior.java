package ro.fortsoft.wicket.pivot.web;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;

import com.google.gson.Gson;

/**
 * @author Decebal Suiu
 */
public abstract class SortableAjaxBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	/** Sorted identifiant into the request */
	private static final String JSON_DATA = "data";
	
	public abstract void onSort(AjaxRequestTarget target, Item[] items);
		
    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);

//        String areasId = getComponent().findParent(PivotPanel.class).get("areas").getMarkupId();
//        System.out.println("areasId = " + areasId);
        
        StringBuilder buffer = new StringBuilder();
//        buffer.append("var data = serializeFieldLocations(" + areasId + ");");
        buffer.append("var data = serializeFieldLocations();");
        buffer.append("return {'" + JSON_DATA + "': data};");

        attributes.getDynamicExtraParameters().add(buffer);
    }

	@Override
	protected void respond(AjaxRequestTarget target) {
		String jsonData = getComponent().getRequest().getRequestParameters().getParameterValue(JSON_DATA).toString();
		Item[] items = getItems(jsonData);
		onSort(target, items);
	}

	private Item[] getItems(String jsonData) {
		Gson gson = new Gson();
		Item[] items = gson.fromJson(jsonData, Item[].class);
		/*
		System.out.println(items.length);
		for (Item item : items) {
			System.out.println(item);
		}
		*/
		
		return items;
	}
		
	public static class Item {
				
		public String areaName;
		public int fieldIndex;
		public int sortIndex;
		
		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("Item[");
			buffer.append("areaName = ").append(areaName);
			buffer.append(" fieldIndex = ").append(fieldIndex);
			buffer.append(" sortIndex = ").append(sortIndex);
			buffer.append("]");

			return buffer.toString();
		}

	}
	
}
