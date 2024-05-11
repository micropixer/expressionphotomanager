import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.expressionphotomanager.R

class DeleteConfirmationFragment : DialogFragment() {

    var onDeleteConfirmed: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_delete_confirmation, container, false)
        view.findViewById<TextView>(R.id.buttonDelete).setOnClickListener {
            onDeleteConfirmed?.invoke()
            dismiss() // Close the fragment
        }
        view.findViewById<TextView>(R.id.buttonCancel).setOnClickListener {
            onCancel?.invoke()
            dismiss() // Close the fragment
        }
        return view
    }

    companion object {
        fun newInstance(onDeleteConfirmed: () -> Unit, onCancel: () -> Unit): DeleteConfirmationFragment {
            val fragment = DeleteConfirmationFragment()
            fragment.onDeleteConfirmed = onDeleteConfirmed
            fragment.onCancel = onCancel
            return fragment
        }
    }
}
