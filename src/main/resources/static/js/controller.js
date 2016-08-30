/**
 * The main angularJS controller handling the query submission and serverside paginator
 * 
 * @author Vangelis Kritsotakis
 */

app.controller('queryController', function($scope) {
	$scope.headingTitle = "SPARQL Query";
});

app.controller('rolesController', function($scope) {
	$scope.headingTitle = "Roles List";
});

app.controller("ResultCtrl", [ '$scope', '$http', 'modalService', function($scope, $http, modalService) {
	
	/*
	var modalOptions = {
		closeButtonText: 'Cancel',
        actionButtonText: 'Delete Customer',
        headerText: 'hello',
        bodyText: 'Are you sure you want to delete this customer?'
	};
	*/
	
	var modalOptions = {
		headerText: 'Loading Please Wait...',
		bodyText: 'Your query is under process...'
	};
	
	var modalDefaults = {
		backdrop: true,
		keyboard: true,
		modalFade: true,
		templateUrl: '/views/loadingModal.html'
	};
	
	
	// Alert (danger, warning, success)
	$scope.alerts = [];
	
	// Pagination
	$scope.maxSize = 5;
	//$scope.totalItems = 0;
	$scope.currentPage = 1;
	$scope.itemsPerPage = 20;
	
	/*
	$scope.paginate = function(value) {
		var begin, end, index;
		begin = ($scope.currentPage - 1) * $scope.itemsPerPage;
		end = begin + $scope.itemsPerPage;
		//index = $scope.lastEndPointForm.result.results.bindings.indexOf(value);
		index = $scope.lastEndPointForm.result.results.indexOf(value);
		return (begin <= index && index < end);
	};
	*/
	
	// Server-Side Pagination
	function getData() {
		
		$http({
			'url' : '/paginator_json',
			'method' : 'GET',
			'headers' : {
				'Content-Type' : 'application/json'
			},
			'params' : {page: $scope.currentPage, itemsPerPage: $scope.itemsPerPage}
		}).success(function(data, status, headers, config) {
			// Checking response from the restful Spring controller (not blazegraph)
			if (status == '200') {
				$scope.endpointResult = data.result;
			} 
			else {
		    }
		}).error(function(data, status, headers, config) {
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: " + message2 + "\n" + JSON.stringify({
				data : data
			}));
		});
	}
	
	$scope.pageChanged = function() {
		getData();
	};
	
	
	
	$scope.queryList = [{}];
	
	
	$scope.addRowAsyncAsJSON = function() {
		
		// Modal
		//modalService.showModal({}, modalOptions);
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
		
		var dataObj = {
			query : $scope.query,
			itemsPerPage : $scope.itemsPerPage
		};

		$http({
			'url' : '/executequery_json',
			'method' : 'POST',
			'headers' : {
				'Content-Type' : 'application/json'
			},
			'data' : dataObj
		}).success(function(data, status, headers, config) {
			
			// Hold feedback to show
			$scope.statusRequestInfo = data.statusRequestInfo;
			$scope.queryList.push({ 'query':$scope.query, 'statusRequestInfo':$scope.statusRequestInfo });
			
			// Checking response from the restful Spring controller (not blazegraph)
			if (status == '200') { 
								
				// Close alerts
				$scope.alerts.splice(0);
				
				// Checking the response from blazegraph
				if(data.statusRequestCode == '200') {
					$scope.lastEndPointForm = data;
					$scope.endpointResult = data.result;
					$scope.totalItems = data.totalItems;
					$scope.currentPage = 1;
					$scope.alerts.push({type: 'success', msg: 'The query was submitted succesfuly'});
				}
				else if(data.statusRequestCode == '400') {
					$scope.alerts.push({type: 'danger', msg: data.statusRequestInfo + '. Please check the query for syntax errors and try again.'});
				}
				else {
					$scope.alerts.push({type: 'danger', msg: data.statusRequestInfo});
				}
				//modalInstance.close();
			}
			else {
		        $scope.alerts.push({type: 'danger', msg: 'Oops, we received your request, but there was an error processing it.'});
		    }
			
			modalInstance.close();

			
		}).error(function(data, status, headers, config) {
		    $scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + message + "\n" + JSON.stringify({
				data : data
			}));
			modalInstance.close();
		});
		
		
		
	}
	
} ]);




/* Notes added the
app.controller("ResultCtrl", [ '$scope', '$http', 'modalService', function($scope, $http, modalService) {
used to be 
app.controller("ResultCtrl", [ '$scope', '$http', function($scope, $http) {

added the modalService.js
added the modal html
added the
var modalOptions = {
            closeButtonText: 'Cancel',
            actionButtonText: 'Delete Customer',
            headerText: 'hello',
            bodyText: 'Are you sure you want to delete this customer?'
        };
        
        added the 
        modalService.showModal({}, modalOptions);
*/

