package jp.tamecco.bluescan;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;

/**
 * Created by xiaomi on 15/08/24.
 */
public class ListFragment extends Fragment {

    public static final String TAG = ListFragment.class.getSimpleName();

    public static final int BT_RESULT = 123;

    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private ScanSettings scanSettings;
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;

    PublishSubject<ScanResult> publishSubject = PublishSubject.create();

    public static ListFragment newInstance() {

        Bundle args = new Bundle();

        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = (RecyclerView) view.findViewById(R.id.list_view);
        recyclerView.setLayoutManager(llm);
        recyclerView.setPadding(5, 5, 5, 5);
        recyclerView.setVerticalScrollBarEnabled(true);
        listAdapter = new ListAdapter(getActivity(), null);
        listAdapter.setHasStableIds(true);
        recyclerView.setAdapter(listAdapter);


    }

    @Override
    public void onStart() {
        super.onStart();

        publishSubject.asObservable()
                .map(new Func1<ScanResult, IBeaconModel>() {
                    @Override
                    public IBeaconModel call(ScanResult scanResult) {
                        Log.d(TAG, "call received");
                        int rssi = scanResult.getRssi();
                        BluetoothDevice device = scanResult.getDevice();

                        List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanResult.getScanRecord().getBytes());
                        IBeacon iBeacon = null;
                        for (ADStructure structure : structures) {
                            if (structure instanceof IBeacon) {
                                iBeacon = (IBeacon) structure;
                            }
                        }
                        IBeaconModel iBeaconModel = new IBeaconModel();
                        iBeaconModel.modelName = device.getName();
                        iBeaconModel.macAddress = device.getAddress();
                        iBeaconModel.rssi = rssi;
                        if (iBeacon != null) {
                            iBeaconModel.power = iBeacon.getPower();
                            iBeaconModel.uuid = iBeacon.getUUID().toString();
                            iBeaconModel.major = iBeacon.getMajor();
                            iBeaconModel.minor = iBeacon.getMinor();
                        }
                        Log.d(TAG, "call ibeacon;" + iBeacon.toString());
                        return iBeaconModel;
                    }
                })

                .distinct(new Func1<IBeaconModel, String>() {
                    @Override
                    public String call(IBeaconModel iBeaconModel) {
                        return iBeaconModel.macAddress;
                    }
                })

                .toSortedList(new Func2<IBeaconModel, IBeaconModel, Integer>() {
                    @Override
                    public Integer call(IBeaconModel iBeaconModel, IBeaconModel iBeaconModel2) {
                        String mac1 = iBeaconModel.macAddress;
                        String mac2 = iBeaconModel2.macAddress;
                        return mac1.compareTo(mac2);
                    }
                })

                .subscribe(new Action1<List<IBeaconModel>>() {
                    @Override
                    public void call(List<IBeaconModel> iBeaconModels) {
                        /*Collections.sort(
                                iBeaconModels,
                                new Comparator<IBeaconModel>() {
                                    @Override
                                    public int compare(IBeaconModel lhs, IBeaconModel rhs) {
                                        return lhs.macAddress.compareTo(rhs.macAddress);
                                    }
                                }
                        );*/
                        Log.d(TAG, "call size:" + iBeaconModels.size());
                        listAdapter.setiBeaconModels(iBeaconModels);
                    }
                });

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(TAG, "onScanResult");
                publishSubject.onNext(result);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                List<ScanFilter> scanFilters = new ArrayList<>();

                scanFilters.add(new ScanFilter.Builder().setDeviceName("tLTb1507").build());
                scanFilters.add(new ScanFilter.Builder().setDeviceName("Kontakt").build());
                bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
            } else {
                Intent intentBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBt, BT_RESULT);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(scanCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        scanCallback = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult req:" + requestCode);
        Log.d(TAG, "onActivityResult res:" + resultCode);
        if (BT_RESULT == requestCode && resultCode == -1) {
            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(new ScanFilter.Builder().setDeviceName("tLTb1507").build());
            scanFilters.add(new ScanFilter.Builder().setDeviceName("Kontakt").build());
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        }
    }


}
