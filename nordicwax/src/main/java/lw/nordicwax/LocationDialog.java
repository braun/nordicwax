package lw.nordicwax;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import lw.droid.commons.Helper;
import lw.droid.forms.BindableListAdapterBase;
import lw.droid.forms.LwActivity;
import lw.droid.forms.LwDialog;
import lw.droid.forms.dialogs.LwMessageBox;
import lw.droid.forms.dialogs.LwSpinnerDialog;
import lw.droid.location.LocationHelper;
import lw.droid.location.LocationHelper.ResultHandler;
import lw.droid.location.LocationInfo;
import lw.droid.location.LocationInfo.LocationResult;


public class LocationDialog extends LwDialog<LocationDialog.Model> {

	
	
	public LocationDialog() {
		setLayoutResourceId(R.layout.dialog_setlocation);
		//setModel(new DialogModelBase(null, R.string.dialog_setlocation_title));
	
	}

	CheckBox cbAutoSet;
	TextView lblAddress;
	EditText txtAddress;
	ImageButton btSearch;
	TextView lblResult;
	Button btOk;

	public void bindViews(View rootView) {
		cbAutoSet = (CheckBox)rootView.findViewById(R.id.cbAutoSet);
		lblAddress = (TextView)rootView.findViewById(R.id.lblAddress);
		txtAddress = (EditText)rootView.findViewById(R.id.txtAddress);
		btSearch = (ImageButton)rootView.findViewById(R.id.btSearch);
		lblResult = (TextView)rootView.findViewById(R.id.lblResult);
		btOk = (Button)rootView.findViewById(R.id.btOk);
	}


	@Override
	public void bindData() 
	{
		if(getModel() != null)
		{
			bindLocationResult(getModel().getLocation());
		}
		enableControls();
		
	}
	
	@Override
	public void initViews() {
		btOk.setEnabled(false);
		cbAutoSet.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				enableManualControls(!isChecked);
				if(isChecked)
				{
					try {
						runGeocoding();
					} catch (Throwable e) {
						Log.e("LocationDialog.btSearch.onClick",e.getLocalizedMessage(),e);
					}
				}
			}
		});
		
		btSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				try {
					runReverseGeocoding();
				} catch (Throwable e) {
					Log.e("LocationDialog.btSearch.onClick",e.getLocalizedMessage(),e);
				}
				
			}
		});
		btOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				try {
					dismiss();
				} catch (Throwable e) {
					Log.e("LocationDialog.btOk.onClick",e.getLocalizedMessage(),e);
				}
				
			}
		});
		LocationHelper.init();
	}


	protected void runReverseGeocoding() {
		String locdesc = txtAddress.getText().toString();
		if(Helper.isStringEmpty(locdesc))
		{
			LwMessageBox.show((LwActivity) getActivity(), "Vyplňte pole adresa");
			return;
		}
		lblResult.setHint("Hledám...");
		LocationHelper.postGetLocationFromAddress(locdesc, new ResultHandler() {
			
			@Override
			public void handleResult(final LocationInfo info) {
			
						try {
							;
							bindLocation(info);
						} catch (Throwable e) {
							Log.e("LocationDialog.runReverseGeocoding.handleResult", e.getLocalizedMessage(), e);
						}
						
					
				}});
				
		
	}


	protected void runGeocoding() {
		
		LocationHelper.postGetLocationInfoForLastKnownPosition( new ResultHandler() {
			
			@Override
			public void handleResult(final LocationInfo info) {
			
						try {
							
							bindLocation(info);
						} catch (Throwable e) {
							Log.e("LocationDialog.runGeocoding.handleResult", e.getLocalizedMessage(), e);
						}
						
					
				}});
				
		
	}


	public static class LocationChooser extends LwSpinnerDialog
	{

		/* (non-Javadoc)
		 * @see lw.droid.forms.dialogs.LwSpinnerDialog#onItemSelected(int)
		 */
		@Override
		protected void onItemSelected(int itemId) {
			((LocationDialog) getParentForm()).locationSelected(getAdapter(),itemId);
		}
		
	}
	int locationSelection = 0;
	private void bindLocation(LocationInfo info) {
	
	
		lblResult.setHint(R.string.location_not_choosen);
		lblResult.setText("");
		
		if(info == null)
			return;
		List<LocationResult> results = new ArrayList<LocationInfo.LocationResult>();
		boolean hasResult = info != null && info.hasResult();
		if(hasResult)
			for(LocationResult r: info.getResults())
				if(r.isLocality())
					results.add(r);
		
		if(results.size() == 0)
		{
			LwMessageBox.show((LwActivity) getActivity(), getActivity().getString(R.string.unable_to_get_location_info));
			return;
		}
		
		if(results.size() > 1)
		{
			LwSpinnerDialog.run(new LocationChooser(), this,getModel(),
					R.string.locationdialog_location_spinner_title,
					new BindableListAdapterBase<LocationResult>(
							this.getActivity(),
							info.getResults(),
							R.layout.spinner_location_item) {
				TextView lblname;
				TextView lbladdress;

				void bindViews(View rootView) {
					lblname = (TextView)rootView.findViewById(R.id.locationitem_lblname);
					lbladdress = (TextView)rootView.findViewById(R.id.locationitem_lbladdress);
				}

				@Override
				public void bind(LocationResult row, View view) {
					bindViews(view);
					lblname.setText(row.getName());
					lbladdress.setText(row.getLocator());
					
				}
			},
			locationSelection);
		} else 
	
		if(hasResult)
		{
			getModel().setLocation(results.get(0));
			bindLocationResult(results.get(0));
		}
		enableControls();
	}





	private void enableControls() {
		btOk.setEnabled(this.getModel().getLocation() != null);
		
	}



	protected void enableManualControls(boolean b) {
		btSearch.setEnabled(b);
		txtAddress.setEnabled(b);
		
	}


	

	private void locationSelected(ListAdapter adapter, int idx) {
		locationSelection = idx;
		LocationResult r = (LocationResult) adapter.getItem(idx);
		getModel().setLocation(r);
		bindLocationResult(r);
	}


	private void bindLocationResult(LocationResult r) {
		lblResult.setText("");
		if(r != null)
			lblResult.setText(r.getFormattedAddress());
		enableControls();
	}



	public static class Model extends lw.droid.forms.LwDialog.DialogModelBase
	{

 		LocationResult location;
		public Model(LocationResult location) {
			super(null,R.string.dialog_setlocation_title);
			// TODO Auto-generated constructor stub
			
			this.location = location;
		}
		/**
		 * @return the location
		 */
		public LocationResult getLocation() {
			if(location == null)
				location = ((QuerySetupActivity.Model)this.getParent()).getLocation();
			return location;
		}
		/**
		 * @param location the location to set
		 */
		public void setLocation(LocationResult location) {
			this.location = location;
			((QuerySetupActivity.Model)this.getParent()).setLocation(location);
		}
		
		
		
	}

}
