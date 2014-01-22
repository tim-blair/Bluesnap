$( document ).ready(function() {
	$(".ui-icon").click(function() {
		var game = $(this).attr('data-game');
		var player = $(this).attr('data-player');
		var state = $(this).attr('data-state');
		var iconCls = '';
		var total = $('#gameTotal-' + game);
		var oldTotal = total.text();
		var newTotal = parseInt(oldTotal, 10);
		// move to the next state
		switch(state) {
			case 'yes': state = 'no'; iconCls = 'closethick red'; newTotal--; break;
			case 'no': state = 'maybe'; iconCls = 'help orange'; break;
			default: state = 'yes'; iconCls = 'check green'; newTotal++; break;
		}
		$(this).attr('class', 'ui-icon ui-icon-' + iconCls);
		$(this).attr('data-state', state);
		total.text(newTotal);
		// make the ajax call
		$.post('/availability', {state: state, game: game, player: player});
	});
	$("a.ics").click(function() {
		var me = $(this);
		$.get('/gameIcs?game=' + me.attr('data-game') + '&player=' + me.attr('data-player'));
	});
});
