package com.padmajeet.mgi.techforedu.admin.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class FeedbackCategory {
	@Exclude
	private String id;
	private String category;
	private String instituteId;
	private String status = "A";
	private String creatorId;
	private String modifierId;
	private String creatorType="A";
	private String modifierType="A";
	private Date createdDate = new Date();
	private Date modifiedDate = new Date();

    @Exclude
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getInstituteId() {
		return instituteId;
	}

	public void setInstituteId(String instituteId) {
		this.instituteId = instituteId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getModifierId() {
		return modifierId;
	}

	public void setModifierId(String modifierId) {
		this.modifierId = modifierId;
	}

	public String getCreatorType() {
		return creatorType;
	}

	public void setCreatorType(String creatorType) {
		this.creatorType = creatorType;
	}

	public String getModifierType() {
		return modifierType;
	}

	public void setModifierType(String modifierType) {
		this.modifierType = modifierType;
	}
}
