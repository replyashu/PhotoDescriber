package com.ashu.photodescriber.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ashu.photodescriber.databinding.ItemImageBinding
import com.ashu.photodescriber.repository.db.entity.UserImages
import com.bumptech.glide.Glide
import javax.inject.Inject

class MainAdapter @Inject constructor(): ListAdapter<UserImages, MainAdapter.DataHolder>(diffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataHolder(binding)
    }

    override fun onBindViewHolder(holder: DataHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    inner class DataHolder(private val imgItem: ItemImageBinding): ViewHolder(imgItem.root) {
        fun bind(item: UserImages) {
            var imgToLoad = item.canonicalImage
            if (imgToLoad.isNullOrEmpty()) imgToLoad = item.imgPath
            Glide.with(imgItem.imgUser.context)
                .load(imgToLoad)
                .into(imgItem.imgUser)
            this.imgItem.root.setOnClickListener {
                val intent = Intent(imgItem.root.context, ImageDetails::class.java)
                intent.putExtra("image_url", imgToLoad)
                intent.putExtra("original_image", item.imgPath)
                imgItem.root.context.startActivity(intent)
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<UserImages>() {
            override fun areItemsTheSame(old: UserImages, new: UserImages) = old.id == new.id
            override fun areContentsTheSame(old: UserImages, new: UserImages) = old == new
        }
    }
}