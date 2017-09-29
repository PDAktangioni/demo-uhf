package com.example.uhf;

import com.example.uhf.fragment.UHFKillFragment;
import com.example.uhf.fragment.UHFLockFragment;
import com.example.uhf.fragment.UHFReadFragment;
import com.example.uhf.fragment.UHFReadTagFragment;
import com.example.uhf.fragment.UHFSetFragment;
import com.example.uhf.fragment.UHFWriteFragment;
import com.rscja.deviceapi.RFIDWithUHF;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.utility.StringUtility;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

/**
 * UHF usando demonstração
 *
 * 1, confirme que sua máquina instalou este módulo antes de usar.
 * 2, para o uso normal do módulo precisa de \ libs \ armeabi \ directory libDeviceAPI.so arquivo, enquanto no diretório \ libs \ colocou o arquivo DeviceAPI.jar.
 * 3, na operação do equipamento antes da necessidade de chamar init () para abrir o dispositivo, use a chamada após o livre () para desligar o dispositivo
 *
 *
 * Para obter mais informações sobre como usar a API, consulte a documentação da API
 *
 * @author liuruifeng 6 de agosto de 2014
 */
public class UHFMainActivity extends BaseTabFragmentActivity {

	private final static String TAG = "MainActivity";

	// public Reader mReader;
	public RFIDWithUHF mReader;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);

	        initViewPageData();
	        initViewPager();
	        initTabs();
		
		

		try {
			mReader = RFIDWithUHF.getInstance();
		} catch (Exception ex) {

			Toast.makeText(UHFMainActivity.this, ex.getMessage(),
					Toast.LENGTH_SHORT).show();

			return;
		}

		if (mReader != null) {
			new InitTask().execute();
		}
	}
	
	 @Override
	    protected void initViewPageData() {
	        lstFrg.add(new UHFReadTagFragment());
	        lstFrg.add(new UHFReadFragment());
	        lstFrg.add(new UHFWriteFragment());
	        lstFrg.add(new UHFSetFragment());
	        lstFrg.add(new UHFKillFragment());
	        lstFrg.add(new UHFLockFragment());


	        lstTitles.add(getString(R.string.uhf_msg_tab_scan));
	        lstTitles.add(getString(R.string.uhf_msg_tab_read));
	        lstTitles.add(getString(R.string.uhf_msg_tab_write));
	        lstTitles.add(getString(R.string.uhf_msg_tab_set));
	        lstTitles.add(getString(R.string.uhf_msg_tab_kill));
	        lstTitles.add(getString(R.string.uhf_msg_tab_lock));
	    }



	@Override
	protected void onDestroy() {

		if (mReader != null) {
			mReader.free();
		}
		super.onDestroy();
	}

	/**
	 * 设备上电异步类
	 *
	 * O dispositivo está ligado de forma assíncrona
	 * 
	 * @author liuruifeng 
	 */	
	public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;
		
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			return mReader.init();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mypDialog.cancel();

			if (!result) {
				Toast.makeText(UHFMainActivity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mypDialog = new ProgressDialog(UHFMainActivity.this);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}

	}
	
	

	/**
	 * 验证十六进制输入是否正确
	 * Verifique se a entrada hexadecimal está correta
	 * 
	 * @param str
	 * @return
	 */
	public boolean vailHexInput(String str) {

		if (str == null || str.length() == 0) {
			return false;
		}

		// 长度必须是偶数
		if (str.length() % 2 == 0) {
			return StringUtility.isHexNumberRex(str);
		}

		return false;
	}

}
