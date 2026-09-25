package com.example.photos.ui.features.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.photos.domain.model.Photo
import com.example.photos.ui.navigation.DetailNavigationClose
import com.example.photos.ui.navigation.SharedPhotoKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@Composable
fun PhotoDetailPager(
    photos: List<Photo>,
    initialPhotoId: String,
    onClose: () -> Unit,
    onNearEnd: () -> Unit = {},
    onPageChanged: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    if (photos.isEmpty()) return
    val initialPage = remember(initialPhotoId) {
        photos.indexOfFirst { it.id == initialPhotoId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { photos.size })
    var pagerReady by remember(initialPhotoId) { mutableStateOf(false) }
    var isZoomed by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var dragAnimation by remember { mutableStateOf<Job?>(null) }
    var isDismissing by remember { mutableStateOf(false) }
    val dismissThreshold = with(LocalDensity.current) { 120.dp.toPx() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialPhotoId) {
        pagerState.scrollToPage(photos.indexOfFirst { it.id == initialPhotoId }.coerceAtLeast(0))
        pagerReady = true
    }
    LaunchedEffect(pagerState.currentPage) { isZoomed = false }
    LaunchedEffect(pagerReady, pagerState.settledPage, photos.size) {
        if (pagerReady) photos.getOrNull(pagerState.settledPage)?.id?.let(onPageChanged)
    }
    LaunchedEffect(pagerState.currentPage, photos.size) {
        if (pagerState.currentPage >= photos.lastIndex - 2) onNearEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .onSizeChanged { viewport = it }
            .background(
                Color.Black.copy(
                    alpha = (1f - abs(dragOffset) / viewport.height.coerceAtLeast(1)).coerceIn(0f, 1f),
                ),
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .graphicsLayer { translationY = dragOffset }
                .pointerInput(isZoomed, isDismissing, dismissThreshold) {
                    if (isZoomed || isDismissing) return@pointerInput
                    detectVerticalDragGestures(
                        onDragStart = { dragAnimation?.cancel() },
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            dragOffset += amount
                        },
                        onDragEnd = {
                            val dismissed = abs(dragOffset) >= dismissThreshold
                            val target = if (dismissed) sign(dragOffset) * viewport.height else 0f
                            if (dismissed) isDismissing = true
                            dragAnimation = scope.launch {
                                animate(dragOffset, target, animationSpec = tween(180)) { value, _ ->
                                    dragOffset = value
                                }
                                if (dismissed) onClose()
                            }
                        },
                        onDragCancel = {
                            dragAnimation = scope.launch {
                                animate(dragOffset, 0f, animationSpec = tween(180)) { value, _ ->
                                    dragOffset = value
                                }
                            }
                        },
                    )
                },
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isZoomed && !isDismissing,
                key = { photos[it].id },
            ) { page ->
                val photo = photos[page]
                val imageModifier = if (
                    page == pagerState.currentPage &&
                    sharedTransitionScope != null &&
                    animatedVisibilityScope != null
                ) {
                    with(sharedTransitionScope) {
                        Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(SharedPhotoKey(photo.id)),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                } else {
                    Modifier
                }
                DetailScreen(
                    photo = photo,
                    imageModifier = imageModifier,
                    onZoomChanged = { zoomed ->
                        if (page == pagerState.currentPage) isZoomed = zoomed
                    },
                )
            }
            DetailNavigationClose(onClose = onClose)
        }
    }
}
