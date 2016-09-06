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

app.controller('exploreController', [ '$scope', '$http', 'modalService', '$location', 'submitQueryService', '$routeParams',
                                      function($scope, $http, modalService, $location, submitQueryService, $routeParams) {
	$scope.headingTitle = "Explore Query";
	
	// Modal Configuration
	var modalOptions = {
		headerText: 'Loading Please Wait...',
		bodyText: 'The URI is under process...'
	};
	
	var modalDefaults = {
		backdrop: true,
		keyboard: true,
		modalFade: true,
		templateUrl: '/views/loadingModal.html'
	};
	
	// Pagination
	$scope.pagination = {
		maxSize: 5,
		currentOutgoingPage: 1,
		currentIncomingPage: 1,
		itemsPerPage: 5
	};
		
	$scope.exploreStatus = {
		isOutgoingHeaderOpen: true,
		isIncomingHeaderOpen: true//,
		//isFirstOpen: true,
		//isFirstDisabled: false
	};
	
	$scope.init = $routeParams.init;
	$scope.type = $routeParams.type;
	$scope.uri = decodeURIComponent($routeParams.uri);
	
	// Server-Side Pagination for incoming or outgoing
	function getData(action) {
		
		var pageParams = {};
		if(action == 'outgoing') {
			pageParams = {
				page: $scope.pagination.currentOutgoingPage, 
				itemsPerPage: $scope.pagination.itemsPerPage,
				action: action
			}
		}
		else if(action == 'incoming') {
			pageParams = {
				page: $scope.pagination.currentIncomingPage, 
				itemsPerPage: $scope.pagination.itemsPerPage,
				action: action
			}
		}
		
		submitQueryService.getPageResults(pageParams)
		.success(function(data, status, headers, config) {
			if(action == 'outgoing') {
				$scope.outgoingEndpointResult = data.result;
			}
			else if(action == 'incoming') {
				$scope.incomingEndpointResult = data.result;
			}
		}).error(function(data, status, headers, config) {
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: " + message2 + "\n" + JSON.stringify({
				data : data
			}));
		});
	}	
	
	$scope.pageOutgoingChanged = function() {
		getData('outgoing');
	};
	
	$scope.pageIncomingChanged = function() {
		getData('incoming');
	};
	
	// Retrieving First page for incoming/outgoing together
	$scope.retrieveAsyncOutgoingIncomingURIsJSON = function(type, column) {

	    // Alert (danger, warning, success)
		$scope.alerts = [];
	    
		var dataObj = {
			uri : column,
			itemsPerPage : $scope.pagination.itemsPerPage
		};
		
		// Modal
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
		
		// Post
		submitQueryService.getOutgoingIncomingResults(dataObj)
		.success(function(data, status, headers, config) {
			
			// Checking the response from blazegraph (outgoing)
			if(data.outgoingEndPointForm.statusRequestCode == '200') {
				
				$scope.outgoingEndPointForm = data.outgoingEndPointForm;
				$scope.outgoingEndpointResult = data.outgoingEndPointForm.result;
				$scope.pagination.outgoingTotalItems = data.outgoingEndPointForm.totalItems;
				//alert($scope.pagination.totalItems);
				$scope.pagination.currentOutgoingPage = 1;
				$scope.alerts.push({type: 'success', msg: 'Outgoing URIs have been succesfuly retrieved'});
			}
			else if(data.statusRequestCode == '400') {
				$scope.alerts.push({
					type: 'danger', 
					msg: data.statusRequestInfo + '. An error has been occurred while trying to retrieve the outgoing URIs'});
			}
			else {
				$scope.alerts.push({type: 'danger', msg: data.statusRequestInfo});
			}
			
			// Incoming URIs
			
			// Checking the response from blazegraph (incoming)
			if(data.incomingEndPointForm.statusRequestCode == '200') {
				$scope.incomingEndPointForm = data.incomingEndPointForm;
				$scope.incomingEndpointResult = data.incomingEndPointForm.result;
				$scope.pagination.incomingTotalItems = data.incomingEndPointForm.totalItems;
				$scope.pagination.currentIncomingPage = 1;
				$scope.alerts.push({type: 'success', msg: 'Incoming URIs have been succesfuly retrieved'});
			}
			else if(data.statusRequestCode == '400') {
				$scope.alerts.push({
					type: 'danger', 
					msg: data.statusRequestInfo + '. An error has been occurred while trying to retrieve the incoming URIs'});
			}
			else {
				$scope.alerts.push({type: 'danger', msg: data.statusRequestInfo});
			}
			
			modalInstance.close();
			//$location.path('/explore');
			
		}).error(function(data, status, headers, config) {
		    var message = 'There was a network error. Try again later.';
			alert("failure message: " + message + "\n" + JSON.stringify({
				data : data
			}));
			////modalInstance.close();
		});		
				
	};
	
	// INIT
	if($routeParams.init == 'true') {
		$scope.retrieveAsyncOutgoingIncomingURIsJSON($routeParams.type, decodeURIComponent($routeParams.uri));
		//$scope.welcomeFunc();
	}
	
} ]);

app.controller("ResultCtrl", [ '$scope', '$http', 'modalService', 'submitQueryService', 
                               function($scope, $http, modalService, submitQueryService) {
	
	// The ui-codemirror option
	$scope.cmOption = {
		lineNumbers: true,
		lineWrapping: true,
		indentWithTabs: true,
		mode: 'sparql'
	};
	
	// Modal options
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
	$scope.currentPage = 1;
	$scope.itemsPerPage = 20;
	
	// Server-Side Pagination for query
	function getData() {
		var pageParams = {
			page: $scope.currentPage, 
			itemsPerPage: $scope.itemsPerPage,
			action: 'query'
		}
		
		submitQueryService.getPageResults(pageParams)
		.success(function(data, status, headers, config) {
			$scope.endpointResult = data.result;
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
	
	$scope.submitAsyncQueryJSON = function() {

		var dataObj = {
			query : $scope.query,
			itemsPerPage : $scope.itemsPerPage
		};
		
		if ($scope.query == null || $scope.query == '') {
			$scope.alerts.splice(0); // Close alerts
			$scope.alerts.push({type: 'warning', msg: 'Please enter some text in the query field and try again!'});
		}
		
		// Only if not empty
		else {

			// Modal
			var modalInstance = modalService.showModal(modalDefaults, modalOptions);
			
			submitQueryService.getQueryResults(dataObj)
			.success(function(data, status, headers, config) {
				
				$scope.statusRequestInfo = data.statusRequestInfo;
				$scope.queryList.push({ 'query':$scope.query, 'statusRequestInfo':$scope.statusRequestInfo });
				
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
				modalInstance.close();
			})
			.error(function() {
				$scope.message = 'There was a network error. Try again later.';
				alert("failure message: " + message + "\n" + JSON.stringify({
					data : data
				}));
				modalInstance.close();
			});
			
		}
		
	}
	
} ]);


