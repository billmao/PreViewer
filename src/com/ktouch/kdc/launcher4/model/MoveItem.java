package com.ktouch.kdc.launcher4.model;

import java.io.Serializable;

import android.R.integer;

import com.lidroid.xutils.db.annotation.Id;
/**
 * 
 * @author 朱永男
 *
 */
public class MoveItem implements Serializable {
	private static final long serialVersionUID = 3388701081007512693L;

	@Id
	private int _id;
	//id
	private int mid;
	//正常模式下的item的Drawable Id
	private String img_normal;
	//按下模式下的item的Drawable Id
	private String img_pressed;
	//正常模式下的item的Drawable Id
		private int img_normal_int;
		//按下模式下的item的Drawable Id
		private int img_pressed_int;
	//item的排序字段
	private int orderId;
	//item描述
	private String text_describe;
	//添加模板标识
	private boolean flag_add;
	//允许拖拽标识
	private boolean flag_drag;
	//用户是否设置标题  0 已设置 1未设置
	private int setTitle;
	//资源图片标识
	private boolean flag_delete;
	//图片来源标识
	private boolean flag_resouce;
	//选中状态
	private boolean flag_selected;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getMid() {
		return mid;
	}

	public void setMid(int mid) {
		this.mid = mid;
	}

	public String getImgurl() {
		return img_normal;
	}

	public void setImgurl(String imgurl) {
		this.img_normal = imgurl;
	}

	public String getImgdown() {
		return img_pressed;
	}

	public void setImgdown(String imgdown) {
		this.img_pressed = imgdown;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getText_describe() {
		return text_describe;
	}

	public void setText_describe(String text_describe) {
		this.text_describe = text_describe;
	}

	public boolean isFlag_add() {
		return flag_add;
	}

	public void setFlag_add(boolean flag_add) {
		this.flag_add = flag_add;
	}

	public boolean isFlag_drag() {
		return flag_drag;
	}

	public void setFlag_drag(boolean flag_drag) {
		this.flag_drag = flag_drag;
	}

	public int getSetTitle() {
		return setTitle;
	}

	public void setSetTitle(int setTitle) {
		this.setTitle = setTitle;
	}

	public boolean isFlag_delete() {
		return flag_delete;
	}

	public void setFlag_delete(boolean flag_delet) {
		this.flag_delete = flag_delet;
	}

	public int getImg_normal_int() {
		return img_normal_int;
	}

	public void setImg_normal_int(int img_normal_int) {
		this.img_normal_int = img_normal_int;
	}

	public int getImg_pressed_int() {
		return img_pressed_int;
	}

	public void setImg_pressed_int(int img_pressed_int) {
		this.img_pressed_int = img_pressed_int;
	}

	public boolean isFlag_resouce() {
		return flag_resouce;
	}

	public void setFlag_resouce(boolean flag_resouce) {
		this.flag_resouce = flag_resouce;
	}

	public boolean isFlag_selected() {
		return flag_selected;
	}

	public void setFlag_selected(boolean flag_selected) {
		this.flag_selected = flag_selected;
	}
	
	
	

}
