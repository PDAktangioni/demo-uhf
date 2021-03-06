package com.example.uhf.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.uhf.R;
import com.example.uhf.StringUtils;
import com.example.uhf.UHFMainActivity;
import com.example.uhf.UIHelper;

public class UHFReadTagFragment extends KeyDwonFragment {

	private boolean loopFlag = false;
	private int inventoryFlag = 1;
	private LinearLayout llQValue;
	Handler handler;
	private ArrayList<HashMap<String, String>> tagList;
	SimpleAdapter adapter;

	Button BtClear;
	TextView tv_count;
	RadioGroup RgInventory;
	RadioButton RbInventorySingle;
	RadioButton RbInventoryLoop;
	RadioButton RbInventoryAnti;
	Spinner SpinnerQ;
	Button BtInventory;
	ListView LvTags;
	byte initQ;

	private EditText et_between;
	private LinearLayout llContinuous;

	private UHFMainActivity mContext;

	private HashMap<String, String> map;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("MY", "UHFReadTagFragment.onCreateView");

		return inflater
				.inflate(R.layout.uhf_readtag_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i("MY", "UHFReadTagFragment.onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		mContext = (UHFMainActivity) getActivity();

		tagList = new ArrayList<HashMap<String, String>>();

		BtClear = (Button) mContext.findViewById(R.id.BtClear);
		tv_count = (TextView) mContext.findViewById(R.id.tv_count);
		RgInventory = (RadioGroup) mContext.findViewById(R.id.RgInventory);
		RbInventorySingle = (RadioButton) mContext
				.findViewById(R.id.RbInventorySingle);
		RbInventoryLoop = (RadioButton) mContext
				.findViewById(R.id.RbInventoryLoop);
		RbInventoryAnti = (RadioButton) mContext
				.findViewById(R.id.RbInventoryAnti);
		SpinnerQ = (Spinner) mContext.findViewById(R.id.SpinnerQ);
		BtInventory = (Button) mContext.findViewById(R.id.BtInventory);
		LvTags = (ListView) mContext.findViewById(R.id.LvTags);

		et_between = (EditText) mContext.findViewById(R.id.et_between);

		llContinuous = (LinearLayout) mContext.findViewById(R.id.llContinuous);

		adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
				new String[] { "tagUii", "tagLen", "tagCount", "tagRssi" },
				new int[] { R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
						R.id.TvTagRssi });

		BtClear.setOnClickListener(new BtClearClickListener());
		RgInventory
				.setOnCheckedChangeListener(new RgInventoryCheckedListener());
		BtInventory.setOnClickListener(new BtInventoryClickListener());
		SpinnerQ.setEnabled(false);
		SpinnerQ.setOnItemSelectedListener(new QItemSelectedListener());

		llQValue = (LinearLayout) mContext.findViewById(R.id.llQValue);

		LvTags.setAdapter(adapter);
		clearData();

		Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				// Bundle bundle = msg.getData();
				// String tagEPC = bundle.getString("tagEPC");

				String result = msg.obj + "";
				String[] strs = result.split("@");
				addEPCToList(strs[0],strs[1]);

			}
		};

		SpinnerQ.setSelection(3);
	}

	@Override
	public void onPause() {
		Log.i("MY", "UHFReadTagFragment.onPause");
		super.onPause();

		// 停止识别
		stopInventory();

	}

	/**
	 * 添加EPC到列表中
	 * 
	 * @param epc
	 */
	private void addEPCToList(String epc,String rssi) {
		if (!TextUtils.isEmpty(epc)) {
			int index = checkIsExist(epc);

			map = new HashMap<String, String>();

			map.put("tagUii", epc);
			map.put("tagCount", String.valueOf(1));
			map.put("tagRssi", rssi);

			// mContext.getAppContext().uhfQueue.offer(epc + "\t 1");

			if (index == -1) {
				tagList.add(map);
				LvTags.setAdapter(adapter);
				tv_count.setText("" + adapter.getCount());
			} else {
				int tagcount = Integer.parseInt(
						tagList.get(index).get("tagCount"), 10) + 1;

				map.put("tagCount", String.valueOf(tagcount));

				tagList.set(index, map);

			}

			adapter.notifyDataSetChanged();

		}
	}

	public class BtClearClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			clearData();

		}
	}

	private void clearData() {
		tv_count.setText("0");

		tagList.clear();

		Log.i("MY", "tagList.size " + tagList.size());

		adapter.notifyDataSetChanged();
	}

	public class RgInventoryCheckedListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			llQValue.setVisibility(View.GONE);
			llContinuous.setVisibility(View.GONE);

			if (checkedId == RbInventorySingle.getId()) {
				// 单步识别
				inventoryFlag = 0;
				SpinnerQ.setEnabled(false);
			} else if (checkedId == RbInventoryLoop.getId()) {
				// 单标签循环识别
				inventoryFlag = 1;
				SpinnerQ.setEnabled(false);

				llContinuous.setVisibility(View.VISIBLE);

			} else {
				// 防碰撞识别
				inventoryFlag = 2;
				SpinnerQ.setEnabled(true);
				llContinuous.setVisibility(View.VISIBLE);
				llQValue.setVisibility(View.VISIBLE);
			}
		}
	}

	public class QItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {

			initQ = Byte.valueOf((String) SpinnerQ.getSelectedItem(), 10);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	public class BtInventoryClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			readTag();
		}
	}

	private void readTag() {
		if (BtInventory.getText().equals(
				mContext.getString(R.string.btInventory)))// Etiqueta de identificação
		{
			switch (inventoryFlag) {
			case 0:// 单步
			{

				String strUII = mContext.mReader.inventorySingleTag();
				if (!TextUtils.isEmpty(strUII)) {
					String strEPC = mContext.mReader.convertUiiToEPC(strUII);
					addEPCToList(strEPC,"N/A");

					tv_count.setText("" + adapter.getCount());
				} else {
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_fail);

				}

			}
				break;
			case 1:// 单标签循环
			{

				if (mContext.mReader.startInventoryTag((byte) 0, (byte) 0)) {
					BtInventory.setText(mContext
							.getString(R.string.title_stop_Inventory));
					loopFlag = true;
					setViewEnabled(false);

					new TagThread(StringUtils.toInt(et_between.getText()
							.toString().trim(), 0)).start();
				} else {
					mContext.mReader.stopInventory();
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_open_fail);
				}
			}

				break;
			case 2:// 防碰撞
			{
				if (mContext.mReader.startInventoryTag((byte) 1, initQ)) {
					BtInventory.setText(mContext
							.getString(R.string.title_stop_Inventory));
					loopFlag = true;
					setViewEnabled(false);
					new TagThread(StringUtils.toInt(et_between.getText()
							.toString().trim(), 0)).start();
				} else {
					mContext.mReader.stopInventory();
					UIHelper.ToastMessage(mContext,
							R.string.uhf_msg_inventory_open_fail);
				}
			}
				break;

			default:
				break;
			}
		} else {// 停止识别
			stopInventory();
		}
	}

	private void setViewEnabled(boolean enabled) {
		RbInventorySingle.setEnabled(enabled);
		RbInventoryLoop.setEnabled(enabled);
		RbInventoryAnti.setEnabled(enabled);
		et_between.setEnabled(enabled);
		SpinnerQ.setEnabled(enabled);

	}

	/**
	 * 停止识别
	 */
	private void stopInventory() {

		if (loopFlag) {

			loopFlag = false;

			setViewEnabled(true);

			if (mContext.mReader.stopInventory()) {
				BtInventory.setText(mContext.getString(R.string.btInventory));
			} else {
				UIHelper.ToastMessage(mContext,
						R.string.uhf_msg_inventory_stop_fail);

			}

		}
	}

	/**
	 * 判断EPC是否在列表中
	 * 
	 * @param strEPC
	 *            索引
	 * @return
	 */
	public int checkIsExist(String strEPC) {
		int existFlag = -1;
		if (StringUtils.isEmpty(strEPC)) {
			return existFlag;
		}

		String tempStr = "";
		for (int i = 0; i < tagList.size(); i++) {
			HashMap<String, String> temp = new HashMap<String, String>();
			temp = tagList.get(i);

			tempStr = temp.get("tagUii");

			if (strEPC.equals(tempStr)) {
				existFlag = i;
				break;
			}
		}

		return existFlag;
	}

	class TagThread extends Thread {

		private int mBetween = 80;
		HashMap<String, String> map;

		public TagThread(int iBetween) {
			mBetween = iBetween;
		}

		public void run() {
			String strTid;
			String strResult;

			String[] res = null;

			while (loopFlag) {

				// strUII = mContext.mReader.readUidFormBuffer();
				//
				// Log.i("UHFReadTagFragment", "TagThread uii=" + strUII);
				//
				// if (StringUtils.isNotEmpty(strUII)) {
				// strEPC = mContext.mReader.convertUiiToEPC(strUII);
				//
				// Message msg = handler.obtainMessage();
				// // Bundle bundle = new Bundle();
				// // bundle.putString("tagEPC", strEPC);
				//
				// // msg.setData(bundle);
				//
				// msg.obj = strEPC;
				// handler.sendMessage(msg);
				//
				// }

				res = mContext.mReader.readTagFormBuffer();

				if (res != null) {

					strTid = res[0];
					if (!strTid.equals("0000000000000000")&&!strTid.equals("000000000000000000000000")) {
						strResult = "TID:" + strTid + "\n";
					} else {
						strResult = "";
					}
					Message msg = handler.obtainMessage();
					msg.obj = strResult + "EPC:"
							+ mContext.mReader.convertUiiToEPC(res[1]) + "@"
							+ res[2];
					handler.sendMessage(msg);
				}
				try {
					sleep(mBetween);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	public void myOnKeyDwon() {
		readTag();
	}

}
