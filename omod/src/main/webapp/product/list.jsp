<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="supply.title" /></h2>

<openmrs:require privilege="View Product" otherwise="/login.htm" redirect="/module/pharmacy/product/units/list.form" />

<%@ include file="../template/localheader.jsp"%>
<%--<script>--%>
<%--    if (jQuery) {--%>
<%--        jQuery(document).ready(function (){--%>
<%--            jQuery('.table').DataTable();--%>
<%--        });--%>
<%--    }--%>
<%--</script>--%>


<div class="container-fluid">
    <div class="d-flex justify-content-between">
        <div class="h5 mb-0 fst-italic">Liste des produits</div>
        <openmrs:hasPrivilege privilege="Import Product">
        <div class="card card-fluid">
            <div class="card-header">
                <div class="h6 text-center">Importation des produits</div>
            </div>
            <div class="card-body">
                <form method="POST" enctype="multipart/form-data"
                      action="${pageContext.request.contextPath}/module/supply/product/upload.form">
                    <div class="row">
                        <div class="col-7">
                            <div class="custom-file">
                                <input type="file" class="custom-file-input" id="customFile" name="file">
                                <label class="custom-file-label" for="customFile">Choisir le fichier CSV</label>
                            </div>
                        </div>
                        <div class="col-4">
                            <button class="btn btn-success"><i class="fa fa-upload"></i> Importer</button>
                        </div>
                    </div>
                </form>
            </div>
            <div class="card card-fluid">
                <div class="card-header">
                    <div class="h6 text-center">Importation des produits</div>
                </div>
                <div class="card-body">
                    <form method="POST" enctype="multipart/form-data"
                          action="${pageContext.request.contextPath}/module/supply/product/upload.form">
                        <div class="row">
                            <div class="col-7">
                                <div class="custom-file">
                                    <input type="file" class="custom-file-input" id="customFile2" name="file">
                                    <label class="custom-file-label" for="customFile2">Choisir le fichier CSV</label>
                                </div>
                            </div>
                            <div class="col-4">
                                <button class="btn btn-success"><i class="fa fa-upload"></i> Importer</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            </openmrs:hasPrivilege>
        </div>
    </div>

    <hr>

    <table class="table table-striped table-sm">
        <thead>
        <tr>
            <th>Id</th>
            <th>
                <%--            <spring:message code="pharmacy.name"/>--%>
                Code
            </th>
            <th>
                <%--            <spring:message code="pharmacy.name"/>--%>
                D&eacute;signation
            </th>
            <th>
                <%--            <spring:message code="pharmacy.name"/>--%>
                Unit&eacute;
            </th>
            <th>
                <%--            <spring:message code="pharmacy.name"/>--%>
                D&eacute;signation (Conditionnement)
            </th>
            <th>
                <%--            <spring:message code="pharmacy.name"/>--%>
                Unit&eacute; (Conditionnement)
            </th>
            <th>
                <%--            <spring:message code="pharmacy.regimenProductNumber"/>--%>
                Unite de conversion
            </th>
            <th style="width: 30px"></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="product" items="${ products }">
            <tr>
                <td>${product.productId}</td>
                <td>${product.code}</td>
                <td>${product.retailName}</td>
                <td>${product.productRetailUnit.name}</td>
                <td>${product.wholesaleName}</td>
                <td>${product.productWholesaleUnit.name}</td>
                <td>${product.unitConversion}</td>
                <td>
                    <c:url value="/module/pharmacy/product/edit.form" var="editUrl">
                        <c:param name="id" value="${product.productId}"/>
                    </c:url>
                    <a href="${editUrl}" class="text-info mr-2"><i class="fa fa-edit"></i></a>
                </td>
            </tr>
        </c:forEach>
        </tbody>

    </table>
    <%@ include file="../template/includeScript.jsp"%>
    <%@ include file="/WEB-INF/template/footer.jsp"%>
