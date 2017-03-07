package fpa.projeto.navegador;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
//import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothClientActivity extends FragmentActivity {
	private static final String TAG = "navegador";
	
	//Para conexão à serial por bluetooth, usa-se o SPP UUID padrão da serial
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothDevice device;	
	private OutputStream out;
	private InputStream in;
	private BluetoothSocket socket;	
	private boolean waitMessage;
	private TextView tNome;
	
	@Override
	protected void onCreate(Bundle arg0) {		
		super.onCreate(arg0);
		setContentView(R.layout.activity_client_connect);
		
		//Recupera o dispositico selecionado na lista
		device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		tNome = (TextView)findViewById(R.id.nomeDevice);
		tNome.setText(device.getName() + " - " + device.getAddress());
		//findViewById(R.id.btn_conectar).setOnClickListener(onClickConectar());
		//findViewById(R.id.btn_desconectar).setOnClickListener(onClickDesconectar());
		findViewById(R.id.btn_up).setOnTouchListener(onTouchUp());
		findViewById(R.id.btn_left).setOnTouchListener(onTouchLeft());
		findViewById(R.id.btn_right).setOnTouchListener(onTouchRight());
		findViewById(R.id.btn_down).setOnTouchListener(onTouchDown());
	}
	
	private OnTouchListener onTouchUp() {
		return new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	sendData("U");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	sendData("u");
		        }
				return false;
			}
		};
	}
	
	private OnTouchListener onTouchLeft() {
		return new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	sendData("L");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	sendData("l");
		        }
				return false;
			}
		};
	}
	
	private OnTouchListener onTouchRight() {
		return new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	sendData("R");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	sendData("r");
		        }
				return false;
			}
		};
	}
	
	private OnTouchListener onTouchDown() {
		return new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	sendData("D");
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	sendData("d");
		        }
				return false;
			}
		};
	}
	
	/*
	private OnClickListener onClickConectar() {
		return new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
				onResume();
			}
		};
		
	}
	
	
	private OnClickListener onClickDesconectar() {
		return new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
				if(socket != null) {
					try {
						socket.close();
					} catch(IOException e2) {
						errorExit("Erro Fatal", "Incapaz de finalizar socket: " + e2.getMessage());
					}					
				}
				findViewById(R.id.btn_conectar).setEnabled(true);
				findViewById(R.id.btn_desconectar).setEnabled(false);
				findViewById(R.id.btn_up).setEnabled(false);
				findViewById(R.id.btn_left).setEnabled(false);
				findViewById(R.id.btn_right).setEnabled(false);
				findViewById(R.id.btn_down).setEnabled(false);
			}
		};
	}
	*/	
	
	
	@Override
	protected void onResume() {		
		super.onResume();
		
		Log.d(TAG, "Tentando estabelecer conexão...");
		
		try {
			socket = device.createRfcommSocketToServiceRecord(uuid);
		} catch(IOException e) {
			Log.e(TAG, "Erro ao conectar: " + e.getMessage(), e);
			errorExit("Erro Fatal", "Falha ao criar socket: " + e.getMessage());
		}
		
		//Estabelecendo conexão
		Log.d(TAG, "Conexão remota");
		try {
			socket.connect();
			Log.d(TAG,"Conexão estabelecida. Link de dados aberto");
		} catch(IOException e) {
			try {
				socket.close();
			} catch(IOException e2) {
				errorExit("Erro Fatal", "Incapaz de fechar socket ao falhar conexão: " + e2.getMessage());
			}
		}
		
		//Criando fluxo de dados para comunicação com o servidor
		Log.d(TAG, "Criando Socket");
		try {
			out = socket.getOutputStream();
		} catch (IOException e) {
			errorExit("Erro Fatal", "Falha ao criar fluxo de saída: " + e.getMessage());
		}
		
		//Conexão estabelecida com sucesso
		if(out != null) {
			//findViewById(R.id.btn_conectar).setEnabled(false);
			//findViewById(R.id.btn_desconectar).setEnabled(true);
			findViewById(R.id.btn_up).setEnabled(true);
			findViewById(R.id.btn_left).setEnabled(true);
			findViewById(R.id.btn_right).setEnabled(true);
			findViewById(R.id.btn_down).setEnabled(true);
		}
		
		if(device != null) {
			new ThreadMessageIn().start();
			waitMessage = true;
		}
	}
	
	//Thread para controlar o recebimento de informações do módulo
	class ThreadMessageIn extends Thread {
		@Override
		public void run() {			
			super.run();
			try {
				if(socket != null) {
					in = socket.getInputStream();
					byte[] bytes = new byte[1024];
					int comp, soma = 0;
					String mess1 = "";
					
					//Permanece em loop esperando mensagens
					while(waitMessage) {
						//Lendo mensagem
						comp = in.read(bytes);
						soma += comp;
						mess1 += new String(bytes, 0, comp);									
						
						if(soma > 31) {
							soma = 0;							
							final String mess = new String(mess1);
							mess1 = "";
							atualizarMensagem(mess);
						}
					} 
				}
			} catch(Exception e) {
				Log.e(TAG, "Erro ao receber dados: " + e.getMessage());
				waitMessage = false;
			}
		}
		
		//Exibindo a mensagem recebida pelo módulo
		private void atualizarMensagem(final String s) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
		
		if(out != null) {
			try {
				out.flush();
			} catch(IOException e) {
				errorExit("Erro Fatal", "Falha ao tentar limpar fluxo de saída de dados: " + e.getMessage());
			}
		}
		
		if(socket != null) {			
			try {
				socket.close();
			} catch(IOException e2) {
				errorExit("Erro Fatal", "Falha ao fechar socket: " + e2.getMessage());
			}
		}
		
	}
		
	private void errorExit(String tit, String msn) {
		Toast msg = Toast.makeText(getBaseContext(), tit + " - " + msn, Toast.LENGTH_SHORT);
		msg.show();
		finish();
	}
	
	public void sendData(String message) {
		byte[] mBuffer = message.getBytes();
		
		Log.d(TAG, "Enviando dados: " + message);
		try {
			out.write(mBuffer);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		waitMessage = false;
		try {
			if(out != null) {
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(socket != null) {
				socket.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}





