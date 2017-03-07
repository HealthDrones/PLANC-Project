package fpa.projeto.navegador;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * @author Ricardo Lecheta
 * 
 */
public class BaseRecognitionListener implements RecognitionListener {
	private static final String TAG = "RecognitionListener";

	public void onBeginningOfSpeech() {
		Log.v(TAG, "onBeginningOfSpeech");
	}

	public void onBufferReceived(byte[] buffer) {
		Log.v(TAG, "onBufferReceived: " + buffer.length);
	}

	public void onEndOfSpeech() {
		Log.v(TAG, "onEndOfSpeech");
	}

	public void onError(int error) {
		Log.e(TAG, "onError: " + error + " : " + getError(error));
	}

	public void onEvent(int eventType, Bundle params) {
		Log.v(TAG, "onEvent: " + eventType);
	}

	public void onPartialResults(Bundle partialResults) {
		Log.v(TAG, "onPartialResults: " + partialResults);
	}

	public void onReadyForSpeech(Bundle params) {
		Log.v(TAG, "onReadyForSpeech: " + params);
	}

	public void onResults(Bundle results) {
		Log.v(TAG, "onResults: " + results);
	}

	public void onRmsChanged(float rmsdB) {
		Log.v(TAG, "onRmsChanged: " + rmsdB);
	}
	
	public String getError(int code) {
		if(code == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
			return code + "- ERROR_NETWORK_TIMEOUT";
		}else if(code == SpeechRecognizer.ERROR_NETWORK) {
			return code + "- ERROR_NETWORK";
		}else if(code == SpeechRecognizer.ERROR_AUDIO) {
			return code + "- ERROR_AUDIO";
		}else if(code == SpeechRecognizer.ERROR_SERVER) {
			return code + "- ERROR_SERVER";
		}else if(code == SpeechRecognizer.ERROR_CLIENT) {
			return code + "- ERROR_CLIENT";
		}else if(code == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
			return code + "- ERROR_SPEECH_TIMEOUT";
		}else if(code == SpeechRecognizer.ERROR_NO_MATCH) {
			return code + "- ERROR_NO_MATCH";
		}else if(code == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
			return code + "- ERROR_RECOGNIZER_BUSY";
		}else if(code == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
			return code + "- ERROR_INSUFFICIENT_PERMISSIONS";
		}
		return String.valueOf(code);
	}
}