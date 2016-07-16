package com.ape.transfer.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.ape.backuprestore.PersonalItemData
import com.ape.transfer.R

import java.util.ArrayList

import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by android on 16-7-15.
 */
class OldPhonePickupAdapter(context: Context, private val mListener: OldPhonePickupAdapter.OnItemClickListener) : RecyclerView.Adapter<OldPhonePickupAdapter.ViewHolder>() {

    private var mBackupDataList: ArrayList<PersonalItemData>? = null
    private val mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(context)
        setHasStableIds(true)
        mBackupDataList = ArrayList<PersonalItemData>()
    }

    fun setDatas(backupDataList: ArrayList<PersonalItemData>) {
        mBackupDataList = backupDataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemData = mBackupDataList!![position]
        holder.tvName.setText(itemData.textId)
        holder.tvCount.text = itemData.count.toString()
        holder.ivIcon.setImageResource(itemData.iconId)
        holder.ivSelected.visibility = if (itemData.isSelected) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return mBackupDataList!!.size
    }

    interface OnItemClickListener {
        fun onItemClick(v: View)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var ivIcon: ImageView
        internal var ivSelected: ImageView
        internal var tvCount: TextView
        internal var tvName: TextView

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener(this)
            ivIcon = itemView.findViewById(R.id.iv_icon) as ImageView
            ivSelected = itemView.findViewById(R.id.iv_selected) as ImageView
            tvCount = itemView.findViewById(R.id.tv_count) as TextView
            tvName = itemView.findViewById(R.id.tv_name) as TextView

        }

        override fun onClick(v: View) {
            mListener.onItemClick(v)
        }
    }
}
