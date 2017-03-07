package fpa.projeto.navegador;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AppControles extends DialogFragment {
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     * http://developer.android.com/guide/topics/ui/dialogs.html */
    public interface NoticeDialogListener {
        public void onItemClick(DialogInterface dialog, int which);
        //public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " deve implementar a Interface NoticeDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.mod_controle)
	           .setItems(R.array.controles, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   mListener.onItemClick(dialog, which);
	           }
	    });
        
        return builder.create();
		//return super.onCreateDialog(savedInstanceState);
	}

}
