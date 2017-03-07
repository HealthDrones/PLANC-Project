package fpa.projeto.navegador;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class BTAccelControlActivity extends BluetoothAccelActivity {
		private static final String TAG = "navegador";
		// Posições para desenhar a imagem
		private int dx, dy;
		private MyView myView;
		private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		private BluetoothDevice device;	
		private OutputStream out;
		private BluetoothSocket socket;	

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			myView = new MyView(this);
			setContentView(myView);
			
			//Recupera o dispositico selecionado na lista
			device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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
		public void onSensorChanged(SensorEvent event) {
			// Lê os valores retornados pelo acelerômetro
			float values[] = AjusteSensor.fixAcelerometro(this, event);
			float sensorX = values[0];
			float sensorY = values[1];
			
			//Enviando indicações para o Arduino
			if(sensorX < -2.0) {
				sendData("L");
			} else if(sensorX > 2.0) {
				sendData("R");
			} else if(sensorY < -2.0) {
				sendData("U");
			} else if(sensorY > 2.0) {
				sendData("D");
			} else {
				sendData("r");
			}			

			// Vai incrementando os valores de x e y, para o objeto se mover
			int newdx = dx + (int) sensorX * 10;
			int newdy = dy + (int) sensorY * 10;
			
			int imgW = myView.drawable.getIntrinsicWidth();
			int imgH = myView.drawable.getIntrinsicHeight();

			// Não deixa o valor ficar negativo, ou maior que o tamanho da tela
			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
			if (! (newdx < 0 || newdx + imgW > displayMetrics.widthPixels)) {
				dx = newdx;
			}
			int actionBarH = displayMetrics.heightPixels-myView.getHeight();
			if (! (newdy < 0 || newdy + imgH > displayMetrics.heightPixels - actionBarH)) {
				dy = newdy;
			}

			// Redesenha a view
			myView.invalidate();
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

		public class MyView extends View {
			private Paint paint = new Paint();
			private Drawable drawable;

			public MyView(Context context) {
				super(context);
				// Configura o fundo cinza, e cria a imagem
				paint.setColor(Color.LTGRAY);
				drawable = context.getResources().getDrawable(R.drawable.my_robot);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			}

			@Override
			protected void onDraw(Canvas canvas) {
				super.onDraw(canvas);

				// Desenha o fundo da view (um quadrado cinza)
				paint.setColor(Color.LTGRAY);
				canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
				
				//paint.setColor(Color.BLACK);
				//canvas.drawText("x: " + dx + ", y: " + dy, 10, 10, paint);

				// Desenha a imagem da posição x e y
				canvas.translate(dx, dy);
				drawable.draw(canvas);
			}
		}

}
