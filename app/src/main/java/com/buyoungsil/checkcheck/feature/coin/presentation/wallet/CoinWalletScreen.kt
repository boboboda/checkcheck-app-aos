package com.buyoungsil.checkcheck.feature.coin.presentation.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buyoungsil.checkcheck.feature.coin.domain.model.CoinTransaction
import com.buyoungsil.checkcheck.feature.coin.domain.model.TransactionType
import com.buyoungsil.checkcheck.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 코인 지갑 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinWalletScreen(
    viewModel: CoinWalletViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGiftDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "💰 내 코인",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
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
        floatingActionButton = {
            // 선물하기 버튼 (항상 표시)
            FloatingActionButton(
                onClick = { showGiftDialog = true },
                containerColor = OrangePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.CardGiftcard, "코인 선물하기")
            }
        },
        containerColor = OrangeBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    // 로딩
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = OrangePrimary
                        )
                    }
                }

                uiState.error != null -> {
                    // 에러
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "😢",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "오류가 발생했어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondaryLight
                        )
                    }
                }

                else -> {
                    // 메인 콘텐츠
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 코인 잔액 카드
                        item {
                            CoinBalanceCard(
                                familyCoins = uiState.wallet?.familyCoins ?: 0,
                                rewardCoins = uiState.wallet?.rewardCoins ?: 0,
                                totalCoins = uiState.wallet?.totalCoins ?: 0
                            )
                        }

                        // 코인 내역 헤더
                        item {
                            Text(
                                text = "📊 코인 내역",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }

                        // 거래 내역
                        if (uiState.transactions.isEmpty()) {
                            item {
                                EmptyTransactionsCard()
                            }
                        } else {
                            items(
                                items = uiState.transactions,
                                key = { it.id }
                            ) { transaction ->
                                TransactionItem(
                                    transaction = transaction,
                                    currentUserId = viewModel.currentUserId
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 선물하기 다이얼로그
    if (showGiftDialog) {
        GiftCoinDialog(
            members = uiState.groupMembers,
            currentUserId = viewModel.currentUserId,
            currentBalance = uiState.wallet?.totalCoins ?: 0,
            onDismiss = { showGiftDialog = false },
            onGift = { toUserId, amount, message ->
                viewModel.giftCoins(toUserId, amount, message)
            }
        )
    }
}

/**
 * 코인 잔액 카드
 */
@Composable
private fun CoinBalanceCard(
    familyCoins: Int,
    rewardCoins: Int,
    totalCoins: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            OrangePrimary.copy(alpha = 0.1f),
                            OrangeSecondary.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 총 코인
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "총 코인",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💰",
                        fontSize = 32.sp
                    )
                    Text(
                        text = totalCoins.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }
            }

            HorizontalDivider(color = DividerLight)

            // 상세 내역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 가족 코인
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "👨‍👩‍👧‍👦 가족 코인",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = familyCoins.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(1.dp),
                    color = DividerLight
                )

                // 보상 코인
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏆 보상 코인",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rewardCoins.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }
            }
        }
    }
}

/**
 * 거래 내역 아이템
 */
@Composable
private fun TransactionItem(
    transaction: CoinTransaction,
    currentUserId: String
) {
    val isReceived = transaction.toUserId == currentUserId
    val isSystem = transaction.fromUserId == "system"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isReceived -> OrangePrimary.copy(alpha = 0.15f)
                            else -> TextSecondaryLight.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.type.icon,
                    fontSize = 24.sp
                )
            }

            // 내용
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                Text(
                    text = when {
                        isSystem -> "시스템"
                        isReceived -> "from ${transaction.fromUserName}"
                        else -> "to ${transaction.toUserName}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )

                transaction.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Text(
                    text = formatTimestamp(transaction.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiaryLight
                )
            }

            // 금액
            Text(
                text = if (isReceived) "+${transaction.amount}" else "-${transaction.amount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isReceived) SuccessOrange else TextSecondaryLight
            )
            Text(
                text = "💰",
                fontSize = 20.sp
            )
        }
    }
}

/**
 * 빈 거래 내역 카드
 */
@Composable
private fun EmptyTransactionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📝",
                fontSize = 48.sp
            )
            Text(
                text = "아직 거래 내역이 없어요",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )
        }
    }
}

/**
 * 타임스탬프 포맷
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "방금 전"
        diff < 3600000 -> "${diff / 60000}분 전"
        diff < 86400000 -> "${diff / 3600000}시간 전"
        diff < 604800000 -> "${diff / 86400000}일 전"
        else -> {
            val sdf = SimpleDateFormat("M월 d일", Locale.KOREAN)
            sdf.format(Date(timestamp))
        }
    }
}
