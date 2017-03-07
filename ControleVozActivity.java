package fpa.projeto.navegador;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ControleVozActivity extends FragmentActivity {
	private static final String TAG = "navegador";
	
	private Button btnSpeak;
	private Button btnAbort;
	private SpeechRecognizer stt;
	
	//Para conexão à serial por bluetooth, usa-se o SPP UUID padrão da serial
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothDevice device;	
	private OutputStream out;
	private BluetoothSocket socket;	
	private TextView txtUp, txtDown, txtLeft, txtRight, txtStop;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_stt);
		
		//Recupera o dispositico selecionado na lista
		device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		
		btnSpeak = (Button)findViewById(R.id.btSpeak);
		btnAbort = (Button)findViewById(R.id.btAbort);
		
		btnAbort.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Para módulo alvo por envio direto de comando.
				sendData("u");
				
			}
		});
		
		txtUp = (TextView)findViewById(R.id.comm_up);
		txtUp.setText("Avançar");
		txtDown = (TextView)findViewById(R.id.comm_down);
		txtDown.setText("Voltar");
		txtLeft = (TextView)findViewById(R.id.comm_left);
		txtLeft.setText("Esquerda");
		txtRight = (TextView)findViewById(R.id.comm_right);
		txtRight.setText("Direita");
		txtStop = (TextView)findViewById(R.id.comm_stop);
		txtStop.setText("Parado");
		
		// Verifica se o Android suporta a intent de reconhecimento de voz
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			btnSpeak.setOnClickListener(onClickSpeak());
			btnSpeak.setEnabled(true);				
			
			stt = SpeechRecognizer.createSpeechRecognizer(this);
			stt.setRecognitionListener(new BaseRecognitionListener(){
				public void onResults(Bundle results) {
					super.onResults(results);
					// Recupera as possívels palavras que foram pronunciadas
					ArrayList<String> comandos = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
					//listView.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, words));
					String comando = comandos.get(0);
					if(comando.contains("up") || comando.contains("avançar")) {
						sendData("U");
					} else if(comando.contains("down") || comando.contains("voltar")) {
						sendData("D");
					} else if(comando.contains("left") || comando.contains("esquerda")) {
						sendData("L");
					} else if(comando.contains("right") || comando.contains("direita")) {
						sendData("R");
					} else if(comando.contains("stop") || comando.contains("parado")){
						sendData("u");
					}
					Toast.makeText(getBaseContext(), "Comando: " + comandos.get(0), Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			Toast.makeText(this, "Reconhecimento de voz  não disponível", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Intent que dispara o reconhecimento de voz
		protected Intent getRecognizerIntent() {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Comando");
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
			return intent;
		}
		
		protected OnClickListener onClickSpeak() {
			return new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Ao clicar no botão recupera a intent e inicia o reconhecimento de voz
					Intent intent = getRecognizerIntent();
					stt.startListening(intent);
				}
			};
		}
		
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
			// Libera os recursos e finaliza o STT
			stt.stopListening();
			stt.destroy();
			
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
