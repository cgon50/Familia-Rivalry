var i;
var answers = []
for(i = 546; i < 2878; i++) {
	if(document.activeElement.children[1].children[0].children[i].children[1]) {
		var cellNode = document.activeElement.children[1].children[0].children[i].children[1]
		var childNum = cellNode.childNodes.length - 1
		while(cellNode.childNodes[childNum] && cellNode.childNodes[childNum].nodeName != "#text" && childNum >= 0) {
			childNum--
    	}
		if(childNum >= 0 && cellNode.childNodes[childNum].textContent != "	") {
		var pushVal = cellNode.childNodes[childNum].textContent
		if(pushVal == "&nbsp;") {
			pushVal = " "
        }
		answers.push(pushVal)
    	}
 	}
}

answers
