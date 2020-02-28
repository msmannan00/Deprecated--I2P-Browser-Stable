<%@page contentType="text/html"%>
<%@page trimDirectiveWhitespaces="true"%>
<%@page pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html><head>
<%@include file="css.jsi" %>
<%=intl.title("help")%>
<script src="/js/ajax.js" type="text/javascript"></script>
<%@include file="summaryajax.jsi" %>
</head><body onload="initAjax()">
<%@include file="summary.jsi" %>
<h1><%=intl._t("I2P Router Help and Support")%></h1>
<div class="main" id="help">
<div class="confignav">
<span class="tab"><a href="#sidebarhelp"><%=intl._t("Sidebar")%></a></span>
<span class="tab"><a href="#reachabilityhelp"><%=intl._t("Reachability")%></a></span>
<span class="tab"><a href="#faq"><%=intl._t("FAQ")%></a></span>
<span class="tab"><a href="/viewlicense"><%=intl._t("Legal")%></a></span>
<span class="tab"><a href="/viewhistory"><%=intl._t("Change Log")%></a></span>
</div>
<div id="volunteer"><%@include file="help.jsi" %></div>
<div id="sidebarhelp"><%@include file="help-sidebar.jsi" %></div>
<div id="reachabilityhelp"><%@include file="help-reachability.jsi" %></div>
<div id="faq"><%@include file="help-faq.jsi" %></div>
</div></body></html>
