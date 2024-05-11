import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.expressionphotomanager.R

class ImageAdapter(
    private val context: Context,
    private val images: List<Pair<Int, ByteArray>>, // Ensure it's a list of pairs
    private val onImageClick: (Int) -> Unit // This expects the image ID
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = BitmapFactory.decodeByteArray(images[position].second, 0, images[position].second.size)
        holder.imageView.setImageBitmap(image)
        holder.itemView.setOnClickListener {
            onImageClick(images[position].first) // Pass the image ID when clicked
        }
    }

    override fun getItemCount() = images.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
}
