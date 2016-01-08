package lw.nordicwax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lw.droid.forms.BindableListAdapterBase;
import lw.droid.forms.LwActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

@EActivity(R.layout.activity_main)
public class MainActivity extends LwActivity<MainActivity.Model> {

	@ViewById(R.id.btOk)
	Button btOk;

	@ViewById(R.id.btRefresh)
	Button btRefresh;

	@ViewById(R.id.lblLocation)
	TextView lblLocation;

	@ViewById(R.id.tabhost)
	TabHost tabhost;

	@ViewById(android.R.id.tabs)
	TabWidget tabs;

	@ViewById(R.id.lblKick)
	TextView lblKick;

	@ViewById(R.id.lblGlide)
	TextView lblGlide;

	@ViewById(android.R.id.tabcontent)
	FrameLayout tabcontent;

	@ViewById(R.id.tabKick)
	ListView tabKick;

	@ViewById(R.id.tabGlide)
	ListView tabGlide;

	@AfterViews
	public void init()
	{
		tabhost.setup();
		
		// Get the original tab textviews and remove them from the viewgroup.
				TextView[] originalTextViews = new TextView[tabs.getTabCount()];
				for (int index = 0; index < tabs.getTabCount(); index++) {
					originalTextViews[index] = (TextView) tabs.getChildTabViewAt(index);
				}
				tabs.removeAllViews();
				
				// Ensure that all tab content childs are not visible at startup.
				for (int index = 0; index < tabcontent.getChildCount(); index++) {
					tabcontent.getChildAt(index).setVisibility(View.GONE);
				}
				
				// Create the tabspec based on the textview childs in the xml file.
				// Or create simple tabspec instances in any other way...
				for (int index = 0; index < originalTextViews.length; index++) {
					final TextView tabsTextView = originalTextViews[index];
					final View tabcontentView = tabcontent.getChildAt(index);
					TabSpec tabSpec = tabhost.newTabSpec((String) tabsTextView.getTag());
					tabSpec.setContent(new TabHost.TabContentFactory() {
						@Override
						public View createTabContent(String tag) {
							return tabcontentView;
						}
					});
					if (tabsTextView.getBackground() == null) {
						tabSpec.setIndicator(tabsTextView.getText());
					} else {
						tabSpec.setIndicator(tabsTextView.getText(), tabsTextView.getBackground());
					}
					
					tabhost.addTab(tabSpec);
				}
				
				tabGlide.setAdapter(new BindableListAdapterBase<Waxing>(this,
						Arrays.asList(
								new Waxing("Swix HF Blue",90),
								new Waxing("Toko LF Grey",80),
								new Waxing("Swix LF Blue",70),
								new Waxing("SkiGo LF Orange",60)),R.layout.listitem) {
									TextView lblWaxName;
									TextView lblProc;
				
									void bindViews(View rootView) {
										lblWaxName = (TextView)rootView.findViewById(R.id.lblWaxName);
										lblProc = (TextView)rootView.findViewById(R.id.lblProc);
									}

									@Override
									public void bind(Waxing row, View view) {
										bindViews(view);
										lblWaxName.setText(row.getName());
										lblProc.setText(row.getReliability() + "%");
									}
				});
	}

	public static void run(Context ctx, QuerySetupActivity.Model model) {
		Model mod = new Model();
		mod.setParent(model);
		runActivity(ctx, MainActivity_.class, mod, null);
		
	}


	public static class Model extends  LwActivity.ModelBase
	{
		List<Waxing> waxings  = new ArrayList<Waxing>();

		/**
		 * @return the waxings
		 */
		public List<Waxing> getWaxings() {
			return waxings;
		}
		
	}

}
