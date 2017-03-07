package fpa.projeto.navegador;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

public class DispositivosBT extends DispositivosPareados {
	
	private ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		//Registra receiver para receber mensagens de dispositivos pareados
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		
		buscarDevices();
	}
	
	private void buscarDevices() {		
		//Vericar se já não existe alguma busca sendo realizada
		if(btfAdapter.isDiscovering()) {
			btfAdapter.cancelDiscovery();
		}
		
		//Dusparando a busca
		btfAdapter.startDiscovery();
		dialog = ProgressDialog.show(this, "Navegador", "Buscando dispositivos bluetooth...",
				false, true);		
	}
	
	//Elemento receiver para receber broadcasts do bluetooth
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		//Quantidade de dispositivos encontrados
		private int count;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			//Dispositivo encontrado
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				//Recuperando device da intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				//Inserindo na lista devices ainda não pareados
				if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
					lista.add(device);
					Toast.makeText(context, "Localizado: " + device.getName() +
							":" + device.getAddress(), Toast.LENGTH_SHORT).show();
					count++;
				}
			} else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				//Busca iniciada
				count = 0;
				Toast.makeText(context, "Busca iniciada.", Toast.LENGTH_SHORT).show();
			} else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				//Busca finalizada
				Toast.makeText(context, "Busca finalizada." + count +
						" dispositivos encontrados", Toast.LENGTH_LONG).show();
				dialog.dismiss();
				
				//Atualizando o listview com todos os dispositivos encontrados.
				atualizaLista();
			}
		}	
	};
	
	protected void onDestroy() {
		super.onDestroy();
		
		//Finalizando a busca ao sair
		if(btfAdapter != null) {
			btfAdapter.cancelDiscovery();
		}
		//Cancela registro do receiver
		this.unregisterReceiver(mReceiver);
	}

}