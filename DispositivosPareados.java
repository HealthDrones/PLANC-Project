package fpa.projeto.navegador;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DispositivosPareados extends cntBluetooth implements OnItemClickListener, AppControles.NoticeDialogListener {
	
	protected List<BluetoothDevice> lista;
	private ListView listView;
	private Intent intent;
	private BluetoothDevice device;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview);
		listView = (ListView)findViewById(R.id.listView);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//Bluetooth já está iniciado
		if(btfAdapter != null) {
			//Listar dispositivos pareados
			lista = new ArrayList<BluetoothDevice>(btfAdapter.getBondedDevices());
			atualizaLista();
		}
	}
	
	protected void atualizaLista() {
		//Criar array com o nome de cada dispositivo
		List<String> nomes = new ArrayList<String>();
		for(BluetoothDevice device : lista) {
			boolean pareado = device.getBondState() == BluetoothDevice.BOND_BONDED;
			nomes.add(device.getName() + " - " + device.getAddress() +
				(pareado ? " - **pareado**" : ""));
		}
		
		//Adapter para popular o ListView
		int layout = android.R.layout.simple_list_item_1;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, layout, nomes);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int idx, long id) {
		//Recuperando o dispositivo selecionado
		device = lista.get(idx);
		//String msn = device.getName() + " - " + device.getAddress();
		//Toast.makeText(this, msn, Toast.LENGTH_SHORT).show();
		
		DialogFragment dialog = new AppControles();
		dialog.show(getSupportFragmentManager(), "Navegador");		

		
		/*
		Intent intent = new Intent(this, BluetoothClientActivity.class);
		intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
		startActivity(intent);
		*/ 
	}
	
	// The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
	// http://developer.android.com/guide/topics/ui/dialogs.html
	@Override
	public void onItemClick(DialogInterface dialog, int which) {
		switch(which) {
			case 0: intent = new Intent(this, BluetoothClientActivity.class); break;
			case 1: intent = new Intent(this, BTAccelControlActivity.class); break;
			case 2: intent = new Intent(this, TouchControlActivity.class); break;
			case 3: intent = new Intent(this, ControleVozActivity.class); break;
			default: Toast.makeText(getBaseContext(), "Opção Incoreta", Toast.LENGTH_SHORT).show(); return;
		}
		
		intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
		startActivity(intent);
	}

}


















