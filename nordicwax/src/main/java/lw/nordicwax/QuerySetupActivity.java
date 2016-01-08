package lw.nordicwax;

import java.util.Arrays;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import lw.droid.LwApplication;
import lw.droid.forms.BindableListAdapterBase;
import lw.droid.forms.LwActivity;
import lw.droid.forms.LwDialog;
import lw.droid.forms.MenuItemDef;
import lw.droid.forms.dialogs.LwMessageBox;
import lw.droid.forms.dialogs.LwSpinnerDialog;
import lw.droid.location.LocationInfo.LocationResult;

@EActivity(R.layout.activity_querysetup)
public class QuerySetupActivity extends LwActivity<QuerySetupActivity.Model> {

	@ViewById(R.id.querysetup_contLocation)
	RelativeLayout contLocation;

	@ViewById(R.id.querysetup_labelLocation)
	TextView labelLocation;

	@ViewById(R.id.querysetup_lblLocation)
	TextView lblLocation;

	@ViewById(R.id.querysetup_lblLocationNotSet)
	TextView lblLocationNotSet;

	@ViewById(R.id.querysetup_contWeather)
	RelativeLayout contWeather;

	@ViewById(R.id.querysetup_labelWeather)
	TextView labelWeather;

	@ViewById(R.id.querysetup_lblTemp)
	TextView lblTemp;

	@ViewById(R.id.querysetup_lblTempNotSet)
	TextView lblTempNotSet;

	@ViewById(R.id.querysetup_contRestPars)
	RelativeLayout contRestPars;

	@ViewById(R.id.querysetup_labelRestPars)
	TextView labelRestPars;

	@ViewById(R.id.querysetup_lblActivityType)
	TextView lblActivityType;

	@ViewById(R.id.querysetup_lblRestParsNotSet)
	TextView lblRestParsNotSet;

	@ViewById(R.id.querysetyp_contWaxPreference)
	RelativeLayout ContWaxPreference;

	@ViewById(R.id.querysetup_labelWaxpreference)
	TextView labelWaxpreference;

	@ViewById(R.id.querysetup_lblPricePrefs)
	TextView lblPricePrefs;

	@ViewById(R.id.querysetup_lblBrands)
	TextView lblBrands;

	@ViewById(R.id.querysetup_lblBrandsNotSet)
	TextView lblBrandsNotSet;

	@ViewById(R.id.querysetup_btSearch)
	Button btSearch;





	@AfterViews
	protected void init()
	{
		if(getModel() == null)
			setModel(new QuerySetupActivity.Model());
		bindModel();
		LwApplication.getLwInstance().setStyle(R.style.AppTheme_DialogTheme);
		contLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try
				{
					LocationDialog dlg = new LocationDialog();
					LocationDialog.Model model =  new LocationDialog.Model(null);
					model.setParent(getModel());
					dlg.setModel(model);
				
					showDialog(dlg);
				}
				catch(Throwable th)
				{
					Log.e("QuerySetupActivity", th.getMessage(), th);
				}
				
			}
		});
		contWeather.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try
				{
					if(getModel().getLocation() == null)
					{
						LwMessageBox.show(QuerySetupActivity.this, getString(R.string.set_location_first));
						return;
					}
					TempDialog dlg = new TempDialog();
					TempDialog.Model model =  new TempDialog.Model();
					model.setParent(getModel());
					dlg.setModel(model);
					
					showDialog(dlg);
				}
				catch(Throwable th)
				{
					Log.e("QuerySetupActivity", th.getMessage(), th);
				}
				
			}
		});
		contRestPars.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try
				{
					
					LwSpinnerDialog.run(new ActivityChooser(), QuerySetupActivity.this, 
							getModel(), new BindableListAdapterBase<ActivityType>(
									QuerySetupActivity.this,
									getModel().getActivityTypes(),R.layout.spinner_item)
									
							{

								@Override
								public void bind(ActivityType row, View view) {
									((TextView)view).setText(row.getTitle());
									
								}
						
							}, 0);
				}
				catch(Throwable th)
				{
					Log.e("QuerySetupActivity.contRestPars.onClick", th.getMessage(), th);
				}
				
			}
		});
		setMenuItems(
			new MenuItemDef(getString(R.string.settings),R.drawable.ic_menu_gear) {
				
				@Override
				public boolean execute(MenuItem item, Context ctx) {
					// TODO Auto-generated method stub
					return false;
				}
			},
			new MenuItemDef(getString(R.string.search),R.drawable.ic_menu_search) {
				
				@Override
				public boolean execute(MenuItem item, Context ctx) {
					// TODO Auto-generated method stub
					return false;
				}
			});
		
	}
	public static class ActivityChooser extends LwSpinnerDialog
	{

		/* (non-Javadoc)
		 * @see lw.droid.forms.dialogs.LwSpinnerDialog#onItemSelected(int)
		 */
		@Override
		protected void onItemSelected(int itemId) {
			((QuerySetupActivity) getParentForm()).activitySelected(getAdapter(),itemId);
		}
		
	}
	protected void bindModel() {
		boolean hasLocation = getModel().getLocation() != null;
		
		lblLocationNotSet.setVisibility(hasLocation ? View.INVISIBLE:View.VISIBLE);
		lblLocation.setVisibility(hasLocation ? View.VISIBLE:View.INVISIBLE);
		if(hasLocation)
			lblLocation.setText(getModel().getLocation().getFormattedAddress());
		else
			lblLocation.setText("");
		
		boolean hasTemp = getModel().getSnowType() != null; 
		lblTempNotSet.setVisibility(hasTemp ? View.INVISIBLE:View.VISIBLE);
		lblTemp.setVisibility(hasTemp ? View.VISIBLE:View.INVISIBLE);
		if(hasTemp)
			lblTemp.setText(String.format("%d°C, %s",getModel().getTemp(),getModel().getSnowType().getTitle()));
		else
			lblTemp.setText("");
		
		boolean hasAct = getModel().getActivityType() != null;
		lblActivityType.setVisibility(hasAct ? View.VISIBLE : View.INVISIBLE);
		lblRestParsNotSet.setVisibility(hasAct ? View.INVISIBLE:View.VISIBLE);
		lblActivityType.setText("");
		if(hasAct)
			lblActivityType.setText(getModel().getActivityType().getTitle());
	}
	

	public void activitySelected(ListAdapter adapter, int itemId) {
		try {
			getModel().setActivityType((ActivityType) adapter.getItem(itemId));
			bindModel();
		} catch (Throwable e) {
			Log.e("QuerySetup.activitySelected", e.getLocalizedMessage(), e);
		}
		
	}
	public static class Model extends LwActivity.ModelBase
	{
		LocationResult location;
		
		/**
		 * @return the location
		 */
		public LocationResult getLocation() {
			return location;
		}
		
		List<ActivityType> actypes = null;
		public List<ActivityType> getActivityTypes() {
			if(actypes == null)
				actypes = Arrays.asList(
					new ActivityType("Turistika"),
					new ActivityType("Tréning"),
					new ActivityType("Závod"));
			
			return actypes;
		}
		/**
		 * @param location the location to set
		 */
		public void setLocation(LocationResult location) {
			this.location = location;
			
		}
		
		int offset;
		int temp;
		SnowType snowType;
		/**
		 * @return the offset
		 */
		public int getOffset() {
			return offset;
		}
		/**
		 * @param offset the offset to set
		 */
		public void setOffset(int offset) {
			this.offset = offset;
		}
		/**
		 * @return the temp
		 */
		public int getTemp() {
			return temp;
		}
		/**
		 * @param temp the temp to set
		 */
		public void setTemp(int temp) {
			this.temp = temp;
		}
		/**
		 * @return the snowType
		 */
		public SnowType getSnowType() {
			return snowType;
		}
		/**
		 * @param snowType the snowType to set
		 */
		public void setSnowType(SnowType snowType) {
			this.snowType = snowType;
		}
		
		ActivityType activityType;

		/**
		 * @return the activityType
		 */
		public ActivityType getActivityType() {
			return activityType;
		}
		/**
		 * @param activityType the activityType to set
		 */
		public void setActivityType(ActivityType activityType) {
			this.activityType = activityType;
		}
	}


	/* (non-Javadoc)
	 * @see lw.droid.forms.LwActivity#dialogDismissedCallback(lw.droid.forms.LwDialog)
	 */
	@Override
	public void dialogDismissedCallback(LwDialog dialog) {
			bindModel();
	}
	
	
	@Click
	public void btSearchClicked()
	{
		MainActivity.run(this,getModel());
	}
	
}
