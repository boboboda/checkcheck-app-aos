package com.buyoungsil.checkcheck.feature.group.presentation.upgrade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.feature.group.domain.model.GroupTier
import com.buyoungsil.checkcheck.ui.theme.*

/**
 * 그룹 티어 업그레이드 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeGroupTierScreen(
    viewModel: UpgradeGroupTierViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 업그레이드 성공 시
    LaunchedEffect(uiState.upgradeSuccess) {
        if (uiState.upgradeSuccess) {
            snackbarHostState.showSnackbar("티어 업그레이드 완료!")
            onNavigateBack()
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "그룹 티어 업그레이드",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeBackground,
                    titleContentColor = TextPrimaryLight
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = OrangeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 현재 상태
            CurrentTierCard(
                group = uiState.group,
                wallet = uiState.wallet
            )

            // 다음 티어 안내
            uiState.group?.let { group ->
                val nextTier = group.tier.getNextTier()
                if (nextTier != null) {
                    NextTierCard(
                        currentTier = group.tier,
                        nextTier = nextTier,
                        upgradeCost = group.tier.upgradeCost ?: 0,
                        canAfford = (uiState.wallet?.let { it.familyCoins + it.rewardCoins } ?: 0) >= (group.tier.upgradeCost ?: 0),
                        onUpgrade = { viewModel.upgradeGroupTier() },
                        isLoading = uiState.loading
                    )
                } else {
                    MaxTierCard()
                }
            }

            // 전체 티어 비교표
            TierComparisonCard()
        }
    }
}

@Composable
private fun CurrentTierCard(
    group: com.buyoungsil.checkcheck.feature.group.domain.model.Group?,
    wallet: com.buyoungsil.checkcheck.feature.coin.domain.model.CoinWallet?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "현재 상태",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Divider(color = DividerLight)

            group?.let {
                // 티어 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "현재 티어",
                        fontSize = 14.sp,
                        color = TextSecondaryLight
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = it.tier.icon,
                            fontSize = 20.sp
                        )
                        Text(
                            text = it.tier.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                }

                // 인원 현황
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "현재 인원",
                        fontSize = 14.sp,
                        color = TextSecondaryLight
                    )
                    Text(
                        text = "${it.currentMemberCount()}/${it.maxMembers}명",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (it.currentMemberCount() >= it.maxMembers - 2) {
                            ErrorRed
                        } else {
                            TextPrimaryLight
                        }
                    )
                }
            }

            Divider(color = DividerLight)

            // 보유 코인
            wallet?.let {
                val totalCoins = it.familyCoins + it.rewardCoins
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "보유 코인",
                        fontSize = 14.sp,
                        color = TextSecondaryLight
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "$totalCoins",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NextTierCard(
    currentTier: GroupTier,
    nextTier: GroupTier,
    upgradeCost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = OrangePrimary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${nextTier.icon} ${nextTier.displayName} 티어",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = OrangePrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$upgradeCost",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Divider(color = OrangePrimary.copy(alpha = 0.3f))

            // 혜택
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "업그레이드 혜택",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                BenefitItem(
                    icon = "👥",
                    text = "최대 ${nextTier.maxMembers}명까지 초대",
                    isUpgrade = true,
                    before = currentTier.maxMembers,
                    after = nextTier.maxMembers
                )
            }

            // 업그레이드 버튼
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                enabled = canAfford && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    disabledContainerColor = TextSecondaryLight
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (canAfford) {
                            "업그레이드하기 (${upgradeCost}코인)"
                        } else {
                            "코인이 부족합니다"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!canAfford) {
                Text(
                    text = "💡 습관을 달성하거나 태스크를 완료하여 코인을 모아보세요!",
                    fontSize = 13.sp,
                    color = TextSecondaryLight,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MaxTierCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💎",
                fontSize = 48.sp
            )
            Text(
                text = "최고 티어입니다!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary
            )
            Text(
                text = "더 이상 업그레이드할 수 없습니다",
                fontSize = 14.sp,
                color = TextSecondaryLight
            )
        }
    }
}

@Composable
private fun TierComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "전체 티어 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )

            Divider(color = DividerLight)

            GroupTier.values().forEach { tier ->
                TierComparisonRow(tier)
                if (tier != GroupTier.values().last()) {
                    Divider(color = DividerLight.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun TierComparisonRow(tier: GroupTier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = tier.icon,
                fontSize = 20.sp
            )
            Text(
                text = tier.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight
            )
        }
        Text(
            text = if (tier.maxMembers == Int.MAX_VALUE) {
                "무제한"
            } else {
                "최대 ${tier.maxMembers}명"
            },
            fontSize = 14.sp,
            color = TextSecondaryLight
        )
    }
}

@Composable
private fun BenefitItem(
    icon: String,
    text: String,
    isUpgrade: Boolean = false,
    before: Int? = null,
    after: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Column {
            Text(
                text = text,
                fontSize = 14.sp,
                color = TextPrimaryLight
            )
            if (isUpgrade && before != null && after != null) {
                Text(
                    text = "$before 명 → $after 명",
                    fontSize = 12.sp,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}