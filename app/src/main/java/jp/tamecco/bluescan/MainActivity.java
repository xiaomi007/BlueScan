package jp.tamecco.bluescan;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * Created by xiaomi on 15/08/24.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment, ListFragment.newInstance(), "frag").commit();
    }

}
