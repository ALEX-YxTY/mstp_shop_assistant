package com.meishipintu.assistant.orderdish;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meishipintu.assistant.R;
import com.milai.dao.DishesDao;
import com.milai.model.Dishes;
import com.meishipintu.core.utils.ConstUtil;
import com.milai.utils.StringUtil;
import com.milai.widget.SquareLoableImageView;
import com.meishipintu.core.widget.StickyListView;
import com.meishipintu.core.widget.StickyListView.StickyAdapter;

public class AdapterDishes extends SimpleCursorAdapter implements OnScrollListener, StickyAdapter {

    private Activity context;
    
    private boolean mNoPic = false; 
    
    public static class ViewHolder {
    	SquareLoableImageView livDishThumb;
        TextView tvDishName;
        TextView tvDishPrice;
        TextView tvDishPriceSeperate; 
        TextView tvDishPriceOrig; 
        Button   btnPlus;
        Button   btnSub;
        TextView tvQuantity;
        LinearLayout llSepNor;
        LinearLayout llHeader;
        TextView tvDishType;
        TextView llDummyPress;
        ImageView ivStar1;
        ImageView ivStar2;
        ImageView ivStar3;
        ImageView ivStar4;
        ImageView ivStar5;
    }

    private LayoutInflater inflater;

    public void switchPicDisp(boolean nopic){
    	mNoPic = nopic;
    }
    
    @SuppressWarnings("deprecation")
    public AdapterDishes(Activity context, Cursor c) {
        super(context, -1, c, new String[] {}, new int[] {});
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null || convertView.getTag() == null) {
            viewHolder = new ViewHolder();
            	if (mNoPic){
            		convertView = inflater.inflate(R.layout.item_dishlist_dish_nopic, null);
            	}else{
            		convertView = inflater.inflate(R.layout.item_dishlist_dish, null);
                    viewHolder.livDishThumb =  (SquareLoableImageView) convertView.findViewById(R.id.liv_dish_thumb);            		
            	}
            	viewHolder.llHeader = (LinearLayout)convertView.findViewById(R.id.ll_header);
            	viewHolder.tvDishType = (TextView)convertView.findViewById(R.id.tv_dish_type);
                viewHolder.tvDishName = (TextView) convertView.findViewById(R.id.tv_dish_name);
                viewHolder.tvDishPrice = (TextView) convertView.findViewById(R.id.tv_dish_price);
                viewHolder.tvDishPriceSeperate = (TextView) convertView.findViewById(R.id.tv_dish_price_seperate);  
                viewHolder.tvDishPriceOrig = (TextView) convertView.findViewById(R.id.tv_dish_price_orig);                
                viewHolder.btnPlus = (Button)convertView.findViewById(R.id.btn_dish_plus);
                viewHolder.btnSub = (Button)convertView.findViewById(R.id.btn_dish_sub);
                viewHolder.tvQuantity = (TextView)convertView.findViewById(R.id.tv_dish_quantity);
                viewHolder.ivStar1 = (ImageView) convertView.findViewById(R.id.iv_star1);
                viewHolder.ivStar2 = (ImageView) convertView.findViewById(R.id.iv_star2);
                viewHolder.ivStar3 = (ImageView) convertView.findViewById(R.id.iv_star3);
                viewHolder.ivStar4 = (ImageView) convertView.findViewById(R.id.iv_star4);
                viewHolder.ivStar5 = (ImageView) convertView.findViewById(R.id.iv_star5);
                viewHolder.llSepNor = (LinearLayout)convertView.findViewById(R.id.ll_sep_nor);
                viewHolder.llDummyPress = (TextView)convertView.findViewById(R.id.ll_dummy_press);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        Cursor c ;
        if (needTitle(position)){
        	viewHolder.llHeader.setVisibility(View.VISIBLE);
        	c = (Cursor) getItem(position);
        	String dishType = c.getString(c.getColumnIndex(Dishes.COLUMN_NAME_TYPE_NAME));
        	Log.d("adapter_dish", Integer.toString(position)+":"+dishType);
        	viewHolder.tvDishType.setText(dishType);
        	viewHolder.llSepNor.setVisibility(View.GONE);
        }else{
        	viewHolder.llHeader.setVisibility(View.GONE);
        	viewHolder.llSepNor.setVisibility(View.VISIBLE);
        }
        c = (Cursor) getItem(position);
        String dishName = c.getString(c.getColumnIndex(Dishes.COLUMN_NAME_DISH_NAME));
        viewHolder.tvDishName.setText(dishName);
        
        int dishPriceOrig = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_PRICE_ORIG));
        if (dishPriceOrig == 0){
        	viewHolder.tvDishPriceOrig.setText(R.string.prompts_price_unknown);
        }else {
        	viewHolder.tvDishPriceOrig.setText(context.getString(R.string.unit_rmb)+StringUtil.getPriceString(dishPriceOrig));
        }
        int dishPrice = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_PRICE));
        if (dishPrice == 0){
        	viewHolder.tvDishPrice.setText(R.string.prompts_price_unknown);
        }else {
        	viewHolder.tvDishPrice.setText(context.getString(R.string.unit_rmb)+StringUtil.getPriceString(dishPrice));
        }            
        if(dishPrice!=dishPriceOrig) {
        	viewHolder.tvDishPriceSeperate.setVisibility(View.VISIBLE);
        	viewHolder.tvDishPrice.setVisibility(View.VISIBLE);
        }
        else {      
        	viewHolder.tvDishPriceSeperate.setVisibility(View.INVISIBLE);        	
        	viewHolder.tvDishPrice.setVisibility(View.INVISIBLE);
        }
        
        if (!mNoPic && viewHolder.livDishThumb != null){
	        String dishThumb = ConstUtil.getFileServerUrl(c.getString(c.getColumnIndex(Dishes.COLUMN_NAME_THUMBNAIL)));
	        if(dishThumb!=null && dishThumb.length()>0)
	        	viewHolder.livDishThumb.load(dishThumb);   

//			final int price = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_PRICE));
//			final int priceOrig = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_PRICE_ORIG));				
//			final String content = c.getString(c
//					.getColumnIndex(Dishes.COLUMN_NAME_CONTENT));
			String pictemp = c.getString(c
					.getColumnIndex(Dishes.COLUMN_NAME_PICTURE));
			if (StringUtil.isNullOrEmpty(pictemp)) {
				pictemp = c.getString(c
						.getColumnIndex(Dishes.COLUMN_NAME_THUMBNAIL));
			}	       
			final String pic = pictemp;
			final long dishId=c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_DISH_ID));
			final View view=convertView;
	        viewHolder.livDishThumb.setOnClickListener(new OnClickListener() {        	
	        	@Override
				public void onClick(View v)	{				  
	        		ActDishDetail actDishDetail=new ActDishDetail(context,dishId);
	        		actDishDetail.showAtLocation(view, Gravity.CENTER, 0, 0);
				}				
	        });
        }
        
        int dishQty = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_DISH_QUANTITY));
        if (dishQty > 0){
        	viewHolder.tvQuantity.setVisibility(View.VISIBLE);
        	viewHolder.btnSub.setVisibility(View.VISIBLE);
        }else {
        	viewHolder.btnSub.setVisibility(View.INVISIBLE);
        	viewHolder.tvQuantity.setVisibility(View.INVISIBLE);
        }
        viewHolder.tvQuantity.setText(""+dishQty);
        final long dishId = c.getLong(c.getColumnIndex(Dishes.COLUMN_NAME_DISH_ID));
        viewHolder.btnPlus.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				updateQuantity(dishId, true);
			}
		});
        viewHolder.btnSub.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				updateQuantity(dishId, false);
			}
		});
        viewHolder.llDummyPress.setTag(position);
        viewHolder.llDummyPress.setOnClickListener(mClkListener);
        byte numStars = (byte) c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_STARS));
        updateStars(numStars, viewHolder);
        return convertView;
    }
    private OnClickListener mClkListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ll_dummy_press: {
				Integer pos = (Integer) v.getTag();
				Cursor c = (Cursor)getItem(pos);
				long dishId = c.getLong(c.getColumnIndex(Dishes.COLUMN_NAME_DISH_ID));				
				updateQuantity(dishId, true);
			}
			break;
			}
		}
    };
    private void updateStars(int numStar, ViewHolder vh){
    	vh.ivStar1.setBackgroundResource(R.drawable.bg_star_nor);
    	vh.ivStar2.setBackgroundResource(R.drawable.bg_star_nor);
    	vh.ivStar3.setBackgroundResource(R.drawable.bg_star_nor);
    	vh.ivStar4.setBackgroundResource(R.drawable.bg_star_nor);
    	vh.ivStar5.setBackgroundResource(R.drawable.bg_star_nor);
    	if (numStar >0){
    		vh.ivStar1.setBackgroundResource(R.drawable.bg_star_sel);
    	}
    	numStar--;
    	if (numStar >0){
    		vh.ivStar2.setBackgroundResource(R.drawable.bg_star_sel);
    	}
    	numStar--;
    	if (numStar >0){
    		vh.ivStar3.setBackgroundResource(R.drawable.bg_star_sel);
    	}
    	numStar--;
    	if (numStar >0){
    		vh.ivStar4.setBackgroundResource(R.drawable.bg_star_sel);
    	}
    	numStar--;
    	if (numStar >0){
    		vh.ivStar5.setBackgroundResource(R.drawable.bg_star_sel);
    	}
    }
    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private void updateQuantity(long id, boolean plus){
    	DishesDao.getInstance().updateQuantity(mContext, id, plus);    	
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
    private boolean needTitle(int position) {
        // 第一个肯定是分类
        if (position == 0) {
            return true;
        }
        // 异常处理
        if (position < 0) {
            return false;
        }
        Cursor c=(Cursor)getItem(position);
        int curType = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_TYPE_ID));
        int currentid=c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_DISH_ID));
        c.moveToPrevious();
        int preType = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_TYPE_ID));
        int preid=c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_DISH_ID));
        // 当前item分类名和上一个item分类名不同，则表示两item属于不同分类
        Log.d("needTitle","curPosition"+Integer.toString(position)+":curType"+Integer.toString(curType)+ ":preType"+Integer.toString(preType));
        if (curType != preType) {
            return true;
        }
        return false;
    }
	@Override
	public int getPinnedHeaderState(int position) {
		if (getCount() == 0 || position < 0) {
            return StickyAdapter.PINNED_HEADER_GONE;
        }

        if (isMove(position) == true) {
            return StickyAdapter.PINNED_HEADER_PUSHED_UP;
        }

        return StickyAdapter.PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View headerView, int position, int alpaha) {
		// 设置标题的内容
		Cursor c = (Cursor) getItem(position);
		String curType = c.getString(c.getColumnIndex(Dishes.COLUMN_NAME_TYPE_NAME));
        if (!TextUtils.isEmpty(curType)) {
            TextView headerTextView = (TextView) headerView.findViewById(R.id.header);
            headerTextView.setText(curType );
        }
	}
	private boolean isMove(int position) {
		Cursor c = (Cursor) getItem(position);
        //检查下一条是不是同一分类下的
        boolean lastOne = false;
        if (position < (getCount()-1)){
        	int curType = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_TYPE_ID));
        	c.moveToNext();
        	int nextType = c.getInt(c.getColumnIndex(Dishes.COLUMN_NAME_TYPE_ID));
        	c.moveToPrevious();
        	if (curType != nextType){
        		lastOne = true;
        	}        	
        }
        return lastOne;
    }
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (view instanceof StickyListView) {
            ((StickyListView) view).controlPinnedHeader(firstVisibleItem);
        }
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

}
