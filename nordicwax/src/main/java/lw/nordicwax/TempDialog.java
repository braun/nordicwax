package lw.nordicwax;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lw.nordicwax.TempDialog.Model.ForecastOffset;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import lw.droid.LwApplication;
import lw.droid.commons.Helper;
import lw.droid.forms.BindableListAdapterBase;
import lw.droid.forms.LwActivity.ModelBase;
import lw.droid.forms.LwDialog;
import lw.droid.forms.LwSpinner;
import lw.droid.forms.LwSpinner.OnItemSelectedListener;
import lw.droid.forms.dialogs.LwMessageBox;
import lw.droid.location.LocationInfo.LocationResult;
import lw.droid.location.OpenWeatherForecast;
import lw.droid.location.OpenWeatherForecast.ForecastItem;
import lw.droid.location.WeatherHelper;
import lw.droid.location.WeatherHelper.ForecastHandler;

public class TempDialog extends LwDialog<TempDialog.Model> {

	public TempDialog()
	{
		setLayoutResourceId(R.layout.dialog_settemp);
	}
	
	CheckBox cbAutoSet;
	Button btOk;
	TextView lblTempLabel;
	EditText txtTemp;
	TextView lblSnowLabel;
	lw.droid.forms.LwSpinner spinSnow;
	TextView lblForcastOffsetLabel;
	lw.droid.forms.LwSpinner spinForecastOffset;

	public void bindViews(View rootView) {
		cbAutoSet = (CheckBox)rootView.findViewById(R.id.cbAutoSet);
		btOk = (Button)rootView.findViewById(R.id.btOk);
		lblTempLabel = (TextView)rootView.findViewById(R.id.lblTempLabel);
		txtTemp = (EditText)rootView.findViewById(R.id.txtTemp);
		lblSnowLabel = (TextView)rootView.findViewById(R.id.lblSnowLabel);
		spinSnow = (lw.droid.forms.LwSpinner)rootView.findViewById(R.id.spinSnow);
		lblForcastOffsetLabel = (TextView)rootView.findViewById(R.id.lblForcastOffsetLabel);
		spinForecastOffset = (lw.droid.forms.LwSpinner)rootView.findViewById(R.id.spinForecastOffset);
	}


	@Override
	public void initViews() {
		
		cbAutoSet.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				try {
					enableControls();
					fillTempFromForecast();
			
				} catch (Throwable e) {
					Log.e("TempDialog.cbAutoSet.onCheckedChanged", e.getLocalizedMessage(), e);
				}
			}

			
		});
		spinForecastOffset.setOnItemSelectedChanged(new OnItemSelectedListener() {
			
			@Override
			public void itemSelected(LwSpinner spinner) {
				try {
					Model.ForecastOffset offset = (ForecastOffset) spinner
							.getSelectedItem();
					getModel().setOffset(offset.getOffset());
					enableControls();
					fillTempFromForecast();
				} catch (Throwable e) {
					Log.e("spinForecastOffset.itemSelected", e.getLocalizedMessage(), e);
				}
				
			}
		});
		
		spinSnow.setOnItemSelectedChanged(new OnItemSelectedListener() {
			
			@Override
			public void itemSelected(LwSpinner spinner) {
				try {
					SnowType snow = (SnowType) spinner.getSelectedItem();
					getModel().setSnowType(snow.empty() ? null : snow);
					enableControls();
				} catch (Throwable e) {
					Log.e("spinSnow.itemSelected", e.getLocalizedMessage(), e);
				}
				
			}
		});
		
		btOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					String str = txtTemp.getText().toString();
					if(Helper.isStringEmpty(str))
					{
						LwMessageBox.show(TempDialog.this, getActivity()
								.getString(R.string.temperature_not_filled));
						return;
					}
					int temp = 0;
					
					try {
						temp = Integer.parseInt(str);
					} catch (NumberFormatException e) {
						LwMessageBox.show(TempDialog.this,getActivity().getString(
								R.string.temparature_should_be_number));
						return;
					}
					getModel().setTemp(temp);
					getModel().commitChanges();
					dismiss();
				} catch (Throwable e) {
					Log.e("TempDialog.btOk.onClick", e.getLocalizedMessage(), e);
				}
			}
		});
		
		
	}

	@Override
	public void bindData() {
		spinForecastOffset.setHostingForm(this);
		spinForecastOffset.setAdapter(new BindableListAdapterBase<Model.ForecastOffset>(getActivity(),getModel().getOffsets(),R.layout.spinner_item) {

			@Override
			public void bind(ForecastOffset row, View view) {
				((TextView)view).setText(row.getLabel());
				
			}
		});
		spinSnow.setHostingForm(this);
		spinSnow.setAdapter(new BindableListAdapterBase<SnowType>(getActivity(),getModel().getSnows(),R.layout.spinner_item)
				{

			@Override
			public void bind(SnowType row, View view) {
				((TextView)view).setText(row.getTitle());
				
			}
		});
		txtTemp.setText(Integer.toString(getModel().getTemp()));
		spinForecastOffset.setSelectedItemIndex(getModel().getOffsetIndex());
		spinSnow.setSelectedItemIndex(getModel().getSnowTypeIndex());
		enableControls();
		
	}

	private void enableControls() {
		btOk.setEnabled(getModel().isOk() && !Helper.isStringEmpty(txtTemp.getText().toString()));
		boolean automode = cbAutoSet.isChecked();
		txtTemp.setEnabled(!automode);
		spinForecastOffset.setEnabled(automode);
	}

	private void fillTempFromForecast() {
		LocationResult loc = ((QuerySetupActivity.Model)getModel().getParent()).getLocation();
		if(cbAutoSet.isChecked() && loc != null )
		{
			WeatherHelper.postGetLocationForecast(loc.getGeometry().getLocation().getLocation(),new ForecastHandler()
			{

				@Override
				public void handleResult(OpenWeatherForecast info) {
					if(info == null)
						return;
					long offset = getModel().getOffset()*60*60*1000;
					Date now = new Date();
					Date destDate = new Date(now.getTime()+offset);
					for(ForecastItem it: info.getList())
					{
						Date dt = it.getDate();
						if(dt.after(destDate))
						{
							txtTemp.setText(Long.toString(Math.round(it.getMain().getTemp())));
							return;
						}
					}
					
				}
				
			});
		}
	}
	
	public static class Model extends lw.droid.forms.LwDialog.DialogModelBase
	{
		public  class ForecastOffset
		{
			int offset;

			/**
			 * @return the offset
			 */
			public int getOffset() {
				return offset;
			}

			public ForecastOffset(int offset) {
				super();
				this.offset = offset;
			}
			
			public String getLabel()
			{
				if(offset == 0)
					return LwApplication.getLwInstance().getApplicationContext().getString(R.string.now);
				else
					return String.format("+%dh", getOffset());
			}
		}
	
		List<ForecastOffset> offsets = null;
		List<SnowType> snows = null;
		public Model()
		{
			super(null,R.string.dialog_settemp_title);
			offsets = Arrays.asList(
					new ForecastOffset(0),
					new ForecastOffset(3),
					new ForecastOffset(6),
					new ForecastOffset(9),
					new ForecastOffset(12),
					new ForecastOffset(15),
					new ForecastOffset(18),
					new ForecastOffset(21),
					new ForecastOffset(24));
			snows = Arrays.asList(new SnowType(""),
									new SnowType("Prachový"),
									new SnowType("Zmrzlý"),
									new SnowType("Firn"));
		}
		
		public int getSnowTypeIndex() {
			if(snowType == null)
				return 0;
			for(int i = 0; i < snows.size();i++)
			{
				SnowType tp = snows.get(i);
				if(snowType.equals(tp))
					return i;
			}
			return 0;
		}

		public int getOffsetIndex() {
			for(int i = 0; i < offsets.size(); i++)
			{
				ForecastOffset f = offsets.get(i);
				if(f.getOffset() >= offset)
					return i;
								
			}
			return 0;
		}

		public void commitChanges() {
			((QuerySetupActivity.Model)this.getParent()).setOffset(getOffset());
			((QuerySetupActivity.Model)this.getParent()).setTemp(getTemp());
			((QuerySetupActivity.Model)this.getParent()).setSnowType(getSnowType());
			
		}

		public boolean isOk()
		{
			return getSnowType() != null && !getSnowType().empty();
		}
		/**
		 * @return the offsets
		 */
		public List<ForecastOffset> getOffsets() {
			return offsets;
		}
		/**
		 * @return the snows
		 */
		public List<SnowType> getSnows() {
			return snows;
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

		/* (non-Javadoc)
		 * @see lw.droid.forms.LwActivity.ModelBase#setParent(lw.droid.forms.LwActivity.ModelBase)
		 */
		@Override
		public void setParent(ModelBase parent) {
			// TODO Auto-generated method stub
			super.setParent(parent);
			setSnowType(((QuerySetupActivity.Model)this.getParent()).getSnowType());
			setTemp(((QuerySetupActivity.Model)this.getParent()).getTemp());
			setOffset(((QuerySetupActivity.Model)this.getParent()).getOffset());
		}
	}
}
