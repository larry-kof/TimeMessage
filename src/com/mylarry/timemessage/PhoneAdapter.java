package com.mylarry.timemessage;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mylarry.timemessage.PhoneContact;

public class PhoneAdapter extends BaseAdapter implements Filterable {
	private ArrayFilter mFilter;
	private List<PhoneContact> mList;
	private Context context;
	private ArrayList<PhoneContact> mUnfilteredData;
	
	public PhoneAdapter(List<PhoneContact> mList, Context context) {
		this.mList = mList;
		this.context = context;
	}

	@Override
	public int getCount() {
		
		return mList==null ? 0:mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		if(convertView==null){
			view = View.inflate(context, R.layout.phone_item, null);
			
			holder = new ViewHolder();
			holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
			holder.tv_phone = (TextView) view.findViewById(R.id.tv_phone);
			
			view.setTag(holder);
		}else{
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		
		PhoneContact pc = mList.get(position);
		
		holder.tv_name.setText(pc.contactsName);
		holder.tv_phone.setText("ÊÖ»ú  "+pc.ContactsNumber);
		
		return view;
	}
	
	static class ViewHolder{
		public TextView tv_name;
		public TextView tv_phone;
		public TextView tv_email;
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	private class ArrayFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<PhoneContact>(mList);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<PhoneContact> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<PhoneContact> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<PhoneContact> newValues = new ArrayList<PhoneContact>(count);

                for (int i = 0; i < count; i++) {
                	PhoneContact pc = unfilteredValues.get(i);
                    if (pc != null) {
                        
                    	if(pc.contactsName!=null && pc.contactsName.startsWith(prefixString)){
                    		newValues.add(pc);
                    	}else if(pc.ContactsNumber!=null && pc.ContactsNumber.startsWith(prefixString)){
                    		newValues.add(pc);
                    	}
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			 //noinspection unchecked
            mList = (List<PhoneContact>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
		}
		
	}
}
