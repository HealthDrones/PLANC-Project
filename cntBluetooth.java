package fpa.projeto.navegador;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class cntBluetooth extends FragmentActivity {
	
	protected BluetoothAdapter btfAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		//Acinando Bluetooth adapter
		btfAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btfAdapter == null) {
			Toast.makeText(this, "Bluetooth não está disponível neste aparelho",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		
		//Caso bluetooth não esteja ligado
		if(btfAdapter.isEnabled()) {
			Toast.makeText(this, "Bluetooh OK", Toast.LENGTH_SHORT).show();
		} else {
			Intent habIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(habIntent, 0); 
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		//Vericando se o usuário realmente ativou o bluetooth
		if(resultCode != FragmentActivity.RESULT_OK) {
			Toast.makeText(this, "Por favor, ativar Bluetooth.", Toast.LENGTH_SHORT)
			.show();
		}
	}

}





















