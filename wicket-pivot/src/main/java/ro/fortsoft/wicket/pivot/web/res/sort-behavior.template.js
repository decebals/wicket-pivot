$('#${component}').sortable({
    connectWith: '.fields',
    forcePlaceholderSize: true,
    placeholder: 'pivot-placeholder',
    cursor: 'move',
    opacity: 0.5,
    stop: function(event, ui) {
        ${stopBehavior}
    }
});