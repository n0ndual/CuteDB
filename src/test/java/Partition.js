function partitionByCopy(array, cursor) {
	var smaller = [];
	var bigger = [];
	var target = array[cursor];
	for ( var i = 0; i < array.length; i++) {
		if (i != cursor) {
			if (array[i] <= target) {

				smaller.push(array[i]);
			} else {
				bigger.push(array[i]);
			}
		}
	}

	return smaller.concat(target, bigger);
};
var source = [ 4, 2, 3, 1, 5, 6, 9, 7 ];
var result = [];
result = partitionByCopy(source, 0);
alert(result);

function swapElement(array, source, target) {
	if (source != target) {
		var tmp = array[source];
		array[source] = array[target];
		array[target] = tmp;
	}
}

function partitionInArray(array, cursor) {
	var position = 0;
	// 先计算出分割点应该处在array的哪个位置
	for ( var i = 0; i < array.length; i++) {
		if (array[i] < array[cursor]) {
			position++;
		}
	}
	swapElement(array, position, cursor);
	var i = position - 1;
	var j = position + 1;
	for (; i >= 0; i--) {
		if (array[i] > array[position]) {
			while (array[j] > array[position]) {
				j++;
			}
			swapElement(array, i, j);
			j++;
		}
	}
	return array;
}
var source = [ 4, 2, 3, 1, 5, 6, 9, 7, 1, 1, 1, 1, 100, 73, 2, 2, 3 ];
partitionInArray(source, 0);
//alert(source);

function swapElement(array, source, target) {
	if (source != target) {
		var tmp = array[source];
		array[source] = array[target];
		array[target] = tmp;
	}
}
function partitionForQuickSort(array, cursor) {
	// 先把作为分割点的array[cursor]移动到array[0]
	swapElement(array, cursor, 0);
	var i = 1;
	var j = array.length - 1;
	while (i < j) {
		while (array[i] < array[0]) {
			i++;
		}
		while (array[j] > array[0]) {
			j--;
		}
		if (i < j) {
			swapElement(array, i, j);
			i++;
			j--;
		}
	}
	if (array[i] < array[0]) {
		swapElement(array, 0, i);
	} else {
		swapElement(array, 0, i - 1);
	}
	return array;
}
var source = [ 4, 2, 3, 1, 5, 6, 9, 7, 1, 1, 1, 1, 100, 73, 2, 2, 3 ];
partitionForQuickSort(source, 0);
//alert(source);

if (!Array.prototype.swap) {
	Array.prototype.swap = function(a, b) {
		var temp = this[a];
		this[a] = this[b];
		this[b] = temp;
		return this;
	};
}
var partitionByBubble = function(arr, start, end) {
	var index = start, pivot = arr[start];
	arr.swap(start, end);
	for ( var i = start; i < end; i++) {
		if (arr[i] < pivot) {
			arr.swap(i, index);
			index++;
		}
	}
	arr.swap(index, end);
	return index;
};

// 性能测试
var source = [];
for ( var i = 10000000; i >= 0; i--) {
	source.push(i);
}
var start = new Date().getTime();
partitionByCopy(source, 0, 10000000);
var stop = new Date().getTime();
alert("partitionByCopy used:" + (stop - start) + " ms");

var source1 = [];
for ( var i = 10000000; i >= 0; i--) {
	source1.push(i);
}
var start = new Date().getTime();
partitionForQuickSort(source1, 8);
var stop = new Date().getTime();
alert("partitionForQuickSort used:" + (stop - start) + " ms");

var source2 = [];
for ( var i = 10000000; i >= 0; i--) {
	source2.push(i);
}
var start = new Date().getTime();
partitionInArray(source2, 8);
var stop = new Date().getTime();
alert("partitionByPreAllocation used:" + (stop - start) + " ms");

var source3 = [];
for ( var i = 10000000; i >= 0; i--) {
	source3.push(i);
}
var start = new Date().getTime();
partitionByBubble(source3, 0, 10000000);
var stop = new Date().getTime();
alert("partitionByBubble used:" + (stop - start) + " ms");
