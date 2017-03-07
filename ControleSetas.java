package fpa.projeto.navegador;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ControleSetas extends FragmentActivity {
	private BluetoothDevice device;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0); 
		setContentView(R.layout.activity_layout_setas);
		
		device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		TextView t_nome = (TextView)findViewById(R.id.device_name);
		t_nome.setText(device.getName() + " - " + device.getAddress());
		
		findViewById(R.id.btn_up).setOnClickListener(onClickUP());
		findViewById(R.id.btn_left).setOnClickListener(onClickLEFT()); 
		findViewById(R.id.btn_right).setOnClickListener(onClickRIGHT());
		findViewById(R.id.btn_down).setOnClickListener(onClickDOWN());
	}
	
	private OnClickListener onClickUP() {
		return new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
				
			}
		};
	}
	
	private OnClickListener onClickLEFT() {
		return new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
				
			}
		};
	}
	
	private OnClickListener onClickRIGHT() {
		return new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
				
			}
		};
	}
	
	private OnClickListener onClickDOWN() {
		return new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
				
			}
		};
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
	}
	
	
	

}
