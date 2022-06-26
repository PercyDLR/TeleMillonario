$(document).ready(function () {
	"use strict"; // start of use strict

	/*==============================
	Menu
	==============================*/
	$('.header__btn').on('click', function () {
		$(this).toggleClass('header__btn--active');
		$('.header__nav').toggleClass('header__nav--active');
		$('.body').toggleClass('body--active');
	});

	/*==============================
	Home
	==============================*/

	$('.home__bg .item').each(function () {
		if ($(this).attr("data-bg")) {
			$(this).css({
				'background': 'url(' + $(this).data('bg') + ')',
				'background-position': 'center center',
				'background-repeat': 'no-repeat',
				'background-size': 'cover'
			});
		}
	});

	$('.home__carousel').owlCarousel({
		mouseDrag: false,
		touchDrag: true,
		dots: false,
		loop: true,
		autoplay: true,
		smartSpeed: 600,
		margin: 30,
		responsive: {
			0: {
				items: 2,
			},
			576: {
				items: 2,
			},
			768: {
				items: 3,
			},
			992: {
				items: 4,
			},
			1200: {
				items: 4,
			},
		}
	});
	$('.photo__carousel').owlCarousel({
		loop: true,
		smartSpeed: 600,
		margin: 60,
		items: 1
	});

	$('.home__nav--next').on('click', function () {
		$('.home__carousel, .home__bg, .photo__carousel').trigger('next.owl.carousel');
	});
	$('.home__nav--prev').on('click', function () {
		$('.home__carousel, .home__bg, .photo__carousel').trigger('prev.owl.carousel');
	});

	$(window).on('resize', function () {
		var itemHeight = $('.home__bg').height();
		$('.home__bg .item').css("height", itemHeight + "px");
	});
	$(window).trigger('resize');

	/*==============================
	Tabs
	==============================*/
	$('.content__mobile-tabs-menu li').each(function () {
		$(this).attr('data-value', $(this).text().trim().toLowerCase());
	});

	$('.content__mobile-tabs-menu li').on('click', function () {
		var text = $(this).text().trim();
		var item = $(this);
		var id = item.closest('.content__mobile-tabs').attr('id');
		$('#' + id).find('.content__mobile-tabs-btn input').val(text);
	});

	/*==============================
	Section bg
	==============================*/
	$('.section--bg, .details__bg').each(function () {
		if ($(this).attr("data-bg")) {
			$(this).css({
				'background': 'url(' + $(this).data('bg') + ')',
				'background-position': 'center center',
				'background-repeat': 'no-repeat',
				'background-size': 'cover'
			});
		}
	});

	/*==============================
	Filter
	==============================*/
	$('.filter__item-menu li').each(function () {
		$(this).attr('data-value', $(this).text().toLowerCase());
	});

	$('.filter__item-menu li').on('click', function () {
		var text = $(this).text();
		var item = $(this);
		var id = item.closest('.filter__item').attr('id');
		$('#' + id).find('.filter__item-btn input').val(text);
	});

	/*==============================
	Morelines
	==============================*/
	$('.card__description--details').moreLines({
		linecount: 6,
		baseclass: 'b-description',
		basejsclass: 'js-description',
		classspecific: '_readmore',
		buttontxtmore: "",
		buttontxtless: "",
		animationspeed: 400
	});
})