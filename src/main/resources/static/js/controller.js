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
		itemsPerPage: 10
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
		.then(function(success) {
			if(action == 'outgoing') {
				$scope.outgoingEndpointResult = success.result;
			}
			else if(action == 'incoming') {
				$scope.incomingEndpointResult = success.result;
			}
		}, function (error) {
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: " + message2 + "\n" + JSON.stringify({
				data : error.data
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
		.then(function(success) {
			
			// Checking the response from blazegraph (outgoing)
			if(success.outgoingEndPointForm.statusRequestCode == '200') {
				
				$scope.outgoingEndPointForm = success.outgoingEndPointForm;
				$scope.outgoingEndpointResult = success.outgoingEndPointForm.result;
				$scope.pagination.outgoingTotalItems = success.outgoingEndPointForm.totalItems;
				//alert($scope.pagination.totalItems);
				$scope.pagination.currentOutgoingPage = 1;
				$scope.alerts.push({type: 'success', msg: 'Outgoing URIs have been succesfuly retrieved'});
			}
			else if(success.statusRequestCode == '400') {
				$scope.alerts.push({
					type: 'danger', 
					msg: success.statusRequestInfo + '. An error has been occurred while trying to retrieve the outgoing URIs'});
			}
			else {
				$scope.alerts.push({type: 'danger', msg: success.statusRequestInfo});
			}
			
			// Incoming URIs
			
			// Checking the response from blazegraph (incoming)
			if(success.incomingEndPointForm.statusRequestCode == '200') {
				
				$scope.incomingEndPointForm = success.incomingEndPointForm;
				$scope.incomingEndpointResult = success.incomingEndPointForm.result;
				$scope.pagination.incomingTotalItems = success.incomingEndPointForm.totalItems;
				$scope.pagination.currentIncomingPage = 1;
				$scope.alerts.push({type: 'success', msg: 'Incoming URIs have been succesfuly retrieved'});
			}
			else if(success.statusRequestCode == '400') {
				$scope.alerts.push({
					type: 'danger', 
					msg: success.statusRequestInfo + '. An error has been occurred while trying to retrieve the incoming URIs'});
			}
			else {
				$scope.alerts.push({type: 'danger', msg: success.statusRequestInfo});
			}
			
			modalInstance.close();
			//$location.path('/explore');
			
		},function (error) {
		    var message = 'There was a network error. Try again later.';
			alert("failure message: " + message + "\n" + JSON.stringify({
				data : error.data
			}));
			////modalInstance.close();
		});		
				
	};
	
	// INIT
	
	// Case of explorer
	if($routeParams.init == 'true') {
		
		// Retrieve rdf.base
		submitQueryService.getRdfBase()
		.then(function(success) {
			// Example:			http://localhost:8080/root/E1.CRM_Entity
			// rdfBase:			http://localhost:8080
			// rdfAfterbase:	root
			$scope.rdfBase = success.rdfBase;
			$scope.rdfAfterbase = success.rdfAfterbase;
			
			// Actions regarding explorer:
			$scope.headingTitle = "Explore URIs";
			$scope.retrieveAsyncOutgoingIncomingURIsJSON($routeParams.type, decodeURIComponent($routeParams.uri));
			//$scope.welcomeFunc();
		} ,function (error) {
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: There was a network error. Try again later.\n" + JSON.stringify({
				data : error.data
			}));
		});
	}
	
	// Case of resolver
	if($routeParams.uriToResolve != null) {
		
		// Retrieve rdf.base
		submitQueryService.getRdfBase()
		.then(function(success) {
			// Example:			http://localhost:8080/root/E1.CRM_Entity
			// rdfBase:			http://localhost:8080
			// rdfAfterbase:	root
			$scope.rdfBase = success.rdfBase;
			$scope.rdfAfterbase = success.rdfAfterbase;
			
			// Actions regarding resolver:
			$scope.headingTitle = "URI Resolver";
			// Retrieving incoming / outgoing URIs (first page)
			$scope.retrieveAsyncOutgoingIncomingURIsJSON(5, $scope.rdfBase + '/' + $scope.rdfAfterbase + '/' + $routeParams.uriToResolve);
		} ,function (error) {
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: There was a network error. Try again later.\n" + JSON.stringify({
				data : error.data
			}));
		});
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
		.then(function(success) {
			$scope.endpointResult = success.result;
		} ,function (error) {
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: " + message2 + "\n" + JSON.stringify({
				data : error.data
			}));
		});
	}
	
	$scope.pageChanged = function() {
		getData();
	};
	
	$scope.queryList = [{}];
	
	$scope.submitAsyncQueryJSON = function() {
		
		// First, we retrieve rdf.base
		submitQueryService.getRdfBase()
		.then(function(success) {
			// Example:			http://localhost:8080/root/E1.CRM_Entity
			// rdfBase:			http://localhost:8080
			// rdfAfterbase:	root
			$scope.rdfBase = success.rdfBase;
			$scope.rdfAfterbase = success.rdfAfterbase;
			
			// Then do some Submit stuff
		
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
				.then(function(success) {
					
					$scope.statusRequestInfo = success.statusRequestInfo;
					$scope.queryList.push({ 'query':$scope.query, 'statusRequestInfo':$scope.statusRequestInfo });
					
					// Close alerts
					$scope.alerts.splice(0);
					
					// Checking the response from blazegraph
					if(success.statusRequestCode == '200') {
						$scope.lastEndPointForm = success;
						$scope.endpointResult = success.result;
						$scope.totalItems = success.totalItems;
						$scope.currentPage = 1;
						$scope.alerts.push({type: 'success', msg: 'The query was submitted succesfuly'});
					}
					else if(success.statusRequestCode == '400') {
						$scope.alerts.push({type: 'danger', msg: success.statusRequestInfo + '. Please check the query for syntax errors and try again.'});
					}
					else {
						$scope.alerts.push({type: 'danger', msg: success.statusRequestInfo});
					}
					modalInstance.close();
				}, function (success) {
					$scope.message = 'There was a network error. Try again later.';
					alert("failure message: " + message + "\n" + JSON.stringify({
						data : success
					}));
					modalInstance.close();
				});
				
			}
			
			// Submit stuff Ends here
			
		} ,function (error) {
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: There was a network error. Try again later.\n" + JSON.stringify({
				data : error.data
			}));
		});
		
	}
	
} ]);


