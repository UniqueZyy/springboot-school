package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 反馈表
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@TableName("feedback")
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 反馈类型：1-建议，2-问题，3-投诉，4-其他
     */
    private Integer type;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 提交人ID
     */
    private Long submitterId;

    /**
     * 状态：0-未处理，1-已处理
     */
    private Integer status;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 回复人ID
     */
    private Long replyId;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(Long submitterId) {
        this.submitterId = submitterId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public Long getReplyId() {
        return replyId;
    }

    public void setReplyId(Long replyId) {
        this.replyId = replyId;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }

    @Override
    public String toString() {
        return "Feedback{" +
        "id=" + id +
        ", type=" + type +
        ", content=" + content +
        ", submitterId=" + submitterId +
        ", status=" + status +
        ", submitTime=" + submitTime +
        ", replyContent=" + replyContent +
        ", replyId=" + replyId +
        ", replyTime=" + replyTime +
        "}";
    }
}